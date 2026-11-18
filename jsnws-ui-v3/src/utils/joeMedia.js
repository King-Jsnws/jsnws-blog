/**
 * joe2.0 媒体组件：joe-bilibili / joe-mp3 / joe-music / joe-mlist
 *
 * - B 站视频：直接拼 player.bilibili.com 的 iframe（bvid 严格校验，拒绝任何 URL 注入）
 * - 音频：APlayer 从 public/libs/aplayer 本地加载，只有页面真的出现音频组件时才拉取
 * - 歌单 / 单曲：第三方网易云解析接口，地址集中成常量；请求失败只在组件内提示，不阻塞页面
 */
import { defineJoeComponent, safeSize } from "./joeComponents";

// 网易云解析接口：第三方公开服务，失效时改这里即可
// （joe 原版单曲用的 vvhan 接口现已返回 HTML，统一换成同一个 meting 实例，type=song / playlist 区分）
const NETEASE_API = "https://api.i-meto.com/meting/api";
export const NETEASE_SONG_API = NETEASE_API + "?server=netease&type=song&id=";
export const NETEASE_PLAYLIST_API = NETEASE_API + "?server=netease&type=playlist&id=";

const PLAYER_BASE = "https://player.bilibili.com/player.html";
const APLAYER_BASE = (process.env.BASE_URL || "/").replace(/\/$/, "") + "/libs/aplayer/";

let aplayerPromise = null;

/** APlayer 按需加载（JS + CSS 都走本地） */
function loadAPlayer() {
  if (window.APlayer) {
    return Promise.resolve(window.APlayer);
  }
  if (aplayerPromise) {
    return aplayerPromise;
  }
  aplayerPromise = new Promise((resolve, reject) => {
    if (!document.querySelector('link[data-aplayer="1"]')) {
      const style = document.createElement("link");
      style.rel = "stylesheet";
      style.href = APLAYER_BASE + "APlayer.min.css";
      style.setAttribute("data-aplayer", "1");
      document.head.appendChild(style);
    }
    const script = document.createElement("script");
    script.src = APLAYER_BASE + "APlayer.min.js";
    script.async = true;
    script.onload = () => {
      if (window.APlayer) {
        resolve(window.APlayer);
      } else {
        reject(new Error("APlayer 未就绪"));
      }
    };
    script.onerror = () => reject(new Error("APlayer 加载失败"));
    document.head.appendChild(script);
  }).catch((error) => {
    aplayerPromise = null;
    throw error;
  });
  return aplayerPromise;
}

/** 只允许 http(s) 与站内相对路径 */
function mediaUrl(value) {
  const raw = String(value || "").trim();
  if (!raw) {
    return "";
  }
  const flat = raw.replace(/[\u0000-\u0020\u007f-\u009f]+/g, "");
  if (/^https?:\/\//i.test(flat)) {
    return raw;
  }
  if (/^[a-z][a-z0-9+.\-]*:/i.test(flat) || flat.startsWith("//")) {
    return "";
  }
  return raw;
}

function safeColor(value, fallback) {
  const color = String(value || "").trim();
  if (/^#[0-9a-f]{3,8}$/i.test(color) || /^rgba?\([\d\s.,%]+\)$/i.test(color) || /^[a-z]{3,20}$/i.test(color)) {
    return color;
  }
  return fallback;
}

function isAutoplay(el) {
  const value = String(el.getAttribute("autoplay") || "").toLowerCase();
  return el.hasAttribute("autoplay") && value !== "false" && value !== "0";
}

function showTip(host, message) {
  const tip = document.createElement("div");
  tip.className = "joe_media-tip";
  tip.textContent = message;
  host.replaceChildren(tip);
}

/** 带超时的 fetch，第三方接口不可用时不至于一直转圈 */
function fetchJson(url, timeout) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeout || 8000);
  return fetch(url, {signal: controller.signal})
    .then((response) => {
      if (!response.ok) {
        throw new Error("HTTP " + response.status);
      }
      return response.json();
    })
    .finally(() => clearTimeout(timer));
}

function mountPlayer(host, options, lrcType) {
  const container = document.createElement("span");
  container.className = "_content";
  container.style.display = "block";
  host.replaceChildren(container);
  return loadAPlayer().then((APlayer) => {
    const player = new APlayer(Object.assign({
      container,
      lrcType,
      preload: "none",
      listFolded: false
    }, options));
    host._aplayer = player;
  }).catch(() => {
    showTip(host, "播放器加载失败，请刷新页面重试");
  });
}

defineJoeComponent("joe-bilibili", function () {
  const bvid = String(this.getAttribute("bvid") || "").trim();
  if (!/^BV[0-9A-Za-z]+$/.test(bvid)) {
    showTip(this, "bvid 未填写或格式不正确");
    return;
  }
  const page = parseInt(this.getAttribute("page"), 10);
  const frame = document.createElement("iframe");
  frame.className = "joe_vplayer";
  frame.src = PLAYER_BASE + "?bvid=" + bvid + "&page=" + (page > 0 ? page : 1)
    + "&high_quality=1&danmaku=0";
  frame.setAttribute("allowfullscreen", "true");
  frame.setAttribute("frameborder", "0");
  frame.setAttribute("scrolling", "no");
  frame.style.width = safeSize(this.getAttribute("width"), "100%");
  frame.style.height = safeSize(this.getAttribute("height"), "500px");
  this.replaceChildren(frame);
});

defineJoeComponent("joe-mp3", function () {
  const url = mediaUrl(this.getAttribute("url"));
  if (!url) {
    showTip(this, "音频地址未填写或地址不合法");
    return;
  }
  mountPlayer(this, {
    theme: safeColor(this.getAttribute("theme"), "#1989fa"),
    autoplay: isAutoplay(this),
    audio: [{
      url,
      name: String(this.getAttribute("name") || "").trim() || "音频",
      cover: mediaUrl(this.getAttribute("cover"))
    }]
  }, 0).catch(() => showTip(this, "播放器加载失败，请刷新页面重试"));
}, function () {
  if (this._aplayer && typeof this._aplayer.destroy === "function") {
    this._aplayer.destroy();
    this._aplayer = null;
  }
});

/** 校验第三方返回的音频数组：只保留 http(s) 地址，避免脏数据把播放器搞挂 */
function pickAudioList(payload) {
  if (!Array.isArray(payload)) {
    return [];
  }
  return payload.filter((item) => item && /^https?:\/\//i.test(String(item.url || "")))
    .map((item) => {
      // meting 返回 title / author / pic，旧版接口返回 name / artist / cover，两种都兼容
      const cover = item.cover || item.pic || "";
      const lrc = typeof item.lrc === "string" && /^https?:\/\//i.test(item.lrc) ? item.lrc : "";
      return {
        name: String(item.name || item.title || "").trim() || "未命名音频",
        artist: String(item.artist || item.author || "").trim(),
        url: String(item.url),
        cover: /^https?:\/\//i.test(cover) ? cover : "",
        lrc
      };
    });
}

defineJoeComponent("joe-music", function () {
  const id = String(this.getAttribute("id") || "").trim();
  if (!/^\d+$/.test(id)) {
    showTip(this, "网易云歌曲 ID 未填写或格式不正确");
    return;
  }
  const host = this;
  fetchJson(NETEASE_SONG_API + id)
    .then((payload) => {
      const audio = pickAudioList(payload);
      if (audio.length === 0) {
        throw new Error("empty");
      }
      return mountPlayer(host, {
        theme: safeColor(host.getAttribute("color"), "#1989fa"),
        autoplay: isAutoplay(host),
        audio
      }, 3);
    })
    .catch(() => {
      if (host.isConnected) {
        showTip(host, "歌曲信息获取失败，请稍后重试");
      }
    });
}, function () {
  if (this._aplayer && typeof this._aplayer.destroy === "function") {
    this._aplayer.destroy();
    this._aplayer = null;
  }
});

defineJoeComponent("joe-mlist", function () {
  const id = String(this.getAttribute("id") || "").trim();
  if (!/^\d+$/.test(id)) {
    showTip(this, "网易云歌单 ID 未填写或格式不正确");
    return;
  }
  const host = this;
  fetchJson(NETEASE_PLAYLIST_API + id)
    .then((payload) => {
      const audio = pickAudioList(payload);
      if (audio.length === 0) {
        throw new Error("empty");
      }
      return mountPlayer(host, {
        theme: safeColor(host.getAttribute("color"), "#1989fa"),
        autoplay: isAutoplay(host),
        audio,
        listFolded: true,
        listMaxHeight: "240px"
      }, 3);
    })
    .catch(() => {
      if (host.isConnected) {
        showTip(host, "歌单信息获取失败，请稍后重试");
      }
    });
}, function () {
  if (this._aplayer && typeof this._aplayer.destroy === "function") {
    this._aplayer.destroy();
    this._aplayer = null;
  }
});
