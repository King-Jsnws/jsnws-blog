/**
 * joe2.0 正文自定义标签（Web Components）—— 纯展示 / 交互组件
 *
 * 移植自 halo-theme-joe2.0/source/js/custom.js，去掉 jQuery，改为原生 DOM：
 * - 属性文本一律走 textContent 或白名单校验，避免属性注入
 * - 链接只放行 http(s) 与站内相对路径，javascript: / data: / vbscript: 降级为 "#"
 * - 渲染放在 connectedCallback：v-html 把整段 HTML 插进文档后子节点才齐全
 *
 * 标签清单：joe-mtitle / joe-abtn / joe-anote / joe-copy / joe-cloud / joe-dotted /
 * joe-progress / joe-message / joe-callout / joe-alert / joe-card-default /
 * joe-card-describe / joe-card-list / joe-timeline / joe-collapse / joe-tabs /
 * joe-gird / joe-lamp / joe-read-limited / joe-hide / joe-raw-content
 */
import { ElMessage } from "element-plus";

const CLOUD_TYPES = {
  "default": "默认网盘",
  "360": "360网盘",
  "bd": "百度网盘",
  "ty": "天翼网盘",
  "ct": "城通网盘",
  "wy": "微云网盘",
  "github": "Github仓库",
  "gitee": "Gitee仓库",
  "lz": "蓝奏云网盘"
};

const MESSAGE_TYPES = ["success", "info", "warning", "error"];

function pick(value, fallback) {
  const text = value === null || value === undefined ? "" : String(value).trim();
  return text || fallback;
}

/** 链接白名单：仅 http/https 与站内相对路径 */
function safeUrl(value) {
  const raw = String(value || "").trim();
  if (!raw) {
    return "#";
  }
  const flat = raw.replace(/[\u0000-\u0020\u007f-\u009f]+/g, "");
  if (/^(javascript|vbscript|data|file):/i.test(flat) || /^[a-z][a-z0-9+.\-]*:\/\//i.test(flat) && !/^https?:\/\//i.test(flat)) {
    return "#";
  }
  return raw;
}

/** 颜色：只接受 #hex / 函数式颜色 / 纯字母色名，其余回落默认值 */
function safeColor(value, fallback) {
  const color = String(value || "").trim();
  if (/^#[0-9a-f]{3,8}$/i.test(color) || /^rgba?\([\d\s.,%]+\)$/i.test(color) || /^[a-z]{3,20}$/i.test(color)) {
    return color;
  }
  return fallback;
}

/** 尺寸：数字 + px/%/rem/em，其余回落默认值 */
export function safeSize(value, fallback) {
  const size = String(value || "").trim();
  return /^\d+(\.\d+)?(px|%|rem|em)?$/.test(size) ? size : fallback;
}

/** FontAwesome 图标：只接受 fa-xxx 形式的类名 */
function safeIcon(value, fallback) {
  const icon = String(value || "").trim();
  return /^fa-[a-z0-9-]+$/i.test(icon) ? icon : fallback;
}

/** 取直属子元素（组件的内容容器） */
function directChild(el, className) {
  for (const item of el.children) {
    if (item.classList.contains(className)) {
      return item;
    }
  }
  return null;
}

/** 内容容器：兼容 joe 的 ._temp 与 ._tpl 两种写法 */
function contentHolder(el) {
  return directChild(el, "_temp") || directChild(el, "_tpl");
}

/** 取出内容容器里的 HTML，并去掉首尾多余的 <br> */
function holderHtml(holder) {
  if (!holder) {
    return "";
  }
  return holder.innerHTML.trim().replace(/^(<br\s*\/?>)|(<br\s*\/?>)$/gi, "").trim();
}

function create(tag, className, text) {
  const node = document.createElement(tag);
  if (className) {
    node.className = className;
  }
  if (text !== undefined) {
    node.textContent = text;
  }
  return node;
}

export function defineJoeComponent(name, render, cleanup) {
  if (customElements.get(name)) {
    return;
  }
  customElements.define(name, class extends HTMLElement {
    connectedCallback() {
      if (this.dataset.joeReady === "1") {
        return;
      }
      this.dataset.joeReady = "1";
      try {
        render.call(this);
      } catch (error) {
        // 单个组件渲染异常不影响整篇文章
        console.warn("[joe]", name, error);
      }
    }
    disconnectedCallback() {
      // 播放器等需要随组件移除一起释放（例如切换文章时）
      if (!cleanup) {
        return;
      }
      try {
        cleanup.call(this);
      } catch (error) {
        console.warn("[joe]", name, error);
      }
    }
  });
}

defineJoeComponent("joe-mtitle", function () {
  const wrap = create("span", "joe_mtitle");
  wrap.appendChild(create("span", "joe_mtitle__text", pick(this.getAttribute("title"), "默认标题")));
  this.replaceChildren(wrap);
});

defineJoeComponent("joe-abtn", function () {
  const color = safeColor(this.getAttribute("color"), "#ff6800");
  const radius = safeSize(this.getAttribute("radius"), "17.5px");
  const icon = safeIcon(this.getAttribute("icon"), "");
  const link = create("a", "joe_abtn");
  link.href = safeUrl(this.getAttribute("href"));
  link.target = "_blank";
  link.rel = "noopener noreferrer nofollow";
  link.style.background = color;
  link.style.borderRadius = radius;
  const iconWrap = create("span", "joe_abtn__icon");
  iconWrap.appendChild(create("i", icon ? "fa " + icon : "fa"));
  link.appendChild(iconWrap);
  link.appendChild(create("span", "joe_abtn__content", pick(this.getAttribute("content"), "多彩按钮")));
  this.replaceChildren(link);
});

defineJoeComponent("joe-anote", function () {
  const type = MESSAGE_TYPES.indexOf(this.getAttribute("type")) > -1 ? this.getAttribute("type") : "secondary";
  const link = create("a", "joe_anote " + type);
  link.href = safeUrl(this.getAttribute("href"));
  link.target = "_blank";
  link.rel = "noopener noreferrer nofollow";
  const iconWrap = create("span", "joe_anote__icon");
  iconWrap.appendChild(create("i", "fa " + safeIcon(this.getAttribute("icon"), "fa-download")));
  link.appendChild(iconWrap);
  link.appendChild(create("span", "joe_anote__content", pick(this.getAttribute("content"), "标签按钮")));
  this.replaceChildren(link);
});

defineJoeComponent("joe-note", function () {
  const box = create("span", "joe_note");
  const iconWrap = create("span", "joe_note__icon");
  iconWrap.appendChild(create("i", "fa " + safeIcon(this.getAttribute("icon"), "fa-sticky-note")));
  const body = create("span", "joe_note__body");
  const title = pick(this.getAttribute("title"), "");
  if (title) {
    body.appendChild(create("strong", "joe_note__title", title));
  }
  const holder = contentHolder(this);
  const content = holder ? holderHtml(holder) : "";
  if (content) {
    const text = create("span", "joe_note__content");
    text.innerHTML = content;
    body.appendChild(text);
  }
  box.appendChild(iconWrap);
  box.appendChild(body);
  this.replaceChildren(box);
});

defineJoeComponent("joe-download", function () {
  const link = create("a", "joe_download");
  link.href = safeUrl(this.getAttribute("href"));
  link.target = "_blank";
  link.rel = "noopener noreferrer nofollow";
  link.appendChild(create("i", "fa " + safeIcon(this.getAttribute("icon"), "fa-download")));
  link.appendChild(create("span", "joe_download__content", pick(this.getAttribute("content"), "点击下载")));
  this.replaceChildren(link);
});

defineJoeComponent("joe-copy", function () {
  const text = pick(this.getAttribute("content"), "默认文本");
  const button = create("span", "joe_copy", pick(this.getAttribute("title"), "点击复制"));
  button.style.fontWeight = this.hasAttribute("bold") ? "bold" : "normal";
  button.style.color = safeColor(this.getAttribute("color"), "inherit");
  button.addEventListener("click", () => {
    const done = () => ElMessage.success("复制成功！");
    const fail = () => ElMessage.error("复制失败，请手动选择文本复制");
    if (navigator.clipboard && window.isSecureContext) {
      navigator.clipboard.writeText(text).then(done).catch(fail);
      return;
    }
    const area = document.createElement("textarea");
    area.value = text;
    area.style.position = "fixed";
    area.style.opacity = "0";
    document.body.appendChild(area);
    area.select();
    try {
      document.execCommand("copy") ? done() : fail();
    } catch (error) {
      fail();
    }
    area.remove();
  });
  this.replaceChildren(button);
});

defineJoeComponent("joe-cloud", function () {
  const type = Object.prototype.hasOwnProperty.call(CLOUD_TYPES, this.getAttribute("type"))
    ? this.getAttribute("type")
    : "default";
  const password = pick(this.getAttribute("password"), "");
  const box = create("span", "joe_cloud");
  box.appendChild(create("div", "joe_cloud__logo _" + type));
  const describe = create("div", "joe_cloud__describe");
  describe.appendChild(create("div", "joe_cloud__describe-title", pick(this.getAttribute("title"), "默认标题")));
  describe.appendChild(create("div", "joe_cloud__describe-type",
    "来源：" + (CLOUD_TYPES[type] || CLOUD_TYPES.default) + (password ? " | 提取码：" + password : "")));
  box.appendChild(describe);
  const link = create("a", "joe_cloud__btn");
  link.href = safeUrl(this.getAttribute("url"));
  link.target = "_blank";
  link.rel = "noopener noreferrer nofollow";
  link.appendChild(create("i", "fa fa-download"));
  box.appendChild(link);
  this.replaceChildren(box);
});

defineJoeComponent("joe-dotted", function () {
  // 属性名在 HTML 里会被小写化，startColor / startcolor 都兼容
  const start = safeColor(this.getAttribute("startColor") || this.getAttribute("startcolor"), "#ff6c6c");
  const end = safeColor(this.getAttribute("endColor") || this.getAttribute("endcolor"), "#1989fa");
  const line = create("span", "joe_dotted");
  line.style.backgroundImage = "repeating-linear-gradient(-45deg, " + start + " 0, " + start
    + " 20%, transparent 0, transparent 25%, " + end + " 0, " + end + " 45%, transparent 0, transparent 50%)";
  this.replaceChildren(line);
});

defineJoeComponent("joe-progress", function () {
  const raw = String(this.getAttribute("percentage") || "").trim();
  const percentage = /^\d{1,3}%$/.test(raw) ? parseInt(raw, 10) + "%" : "50%";
  const color = safeColor(this.getAttribute("color"), "#ff6c6c");
  const box = create("span", "joe_progress");
  const strip = create("div", "joe_progress__strip");
  const bar = create("div", "joe_progress__strip-percent");
  bar.style.width = percentage;
  bar.style.background = color;
  strip.appendChild(bar);
  box.appendChild(strip);
  box.appendChild(create("div", "joe_progress__percentage", percentage));
  this.replaceChildren(box);
});

defineJoeComponent("joe-message", function () {
  const type = MESSAGE_TYPES.indexOf(this.getAttribute("type")) > -1 ? this.getAttribute("type") : "info";
  const box = create("span", "joe_message " + type);
  box.appendChild(create("span", "joe_message__icon"));
  box.appendChild(create("span", "joe_message__content", pick(this.getAttribute("content"), "消息内容")));
  this.replaceChildren(box);
});

/** 带内容容器的块级组件（card-default / card-describe / callout / alert）共用 */
function renderBlock(host, rootClass, html, decorate) {
  const holder = contentHolder(host);
  const root = create("div", rootClass);
  root.innerHTML = html || "";
  if (decorate) {
    decorate(root);
  }
  const wrap = create("span", "_content");
  wrap.style.display = "block";
  wrap.appendChild(root);
  host.replaceChildren(wrap);
  if (holder) {
    holder.remove();
  }
}

defineJoeComponent("joe-card-default", function () {
  const width = safeSize(this.getAttribute("width"), "100%");
  const label = pick(this.getAttribute("label"), "卡片标题");
  const html = holderHtml(contentHolder(this));
  renderBlock(this, "joe_card__default", html || "卡片内容", (root) => {
    root.style.width = width;
    const title = create("div", "joe_card__default-title", label);
    const content = create("div", "joe_card__default-content");
    content.innerHTML = html || "卡片内容";
    root.replaceChildren(title, content);
  });
});

defineJoeComponent("joe-card-describe", function () {
  const title = pick(this.getAttribute("title"), "卡片描述");
  const html = holderHtml(contentHolder(this));
  renderBlock(this, "joe_card__describe", html || "卡片内容", (root) => {
    const head = create("div", "joe_card__describe-title", title);
    const content = create("div", "joe_card__describe-content");
    content.innerHTML = html || "卡片内容";
    root.replaceChildren(head, content);
  });
});

defineJoeComponent("joe-callout", function () {
  const color = safeColor(this.getAttribute("color"), "#f0ad4e");
  const html = holderHtml(contentHolder(this));
  renderBlock(this, "joe_callout", html || "标注内容", (root) => {
    root.style.borderLeftColor = color;
  });
});

defineJoeComponent("joe-alert", function () {
  const type = MESSAGE_TYPES.indexOf(this.getAttribute("type")) > -1 ? this.getAttribute("type") : "info";
  const html = holderHtml(contentHolder(this));
  renderBlock(this, "joe_alert " + type, html || "警告提示");
});

defineJoeComponent("joe-card-list", function () {
  const html = holderHtml(contentHolder(this));
  const list = create("div", "joe_card__list");
  const reg = /\{card-list-item\}([\s\S]*?)\{\/card-list-item\}/g;
  let matched;
  while ((matched = reg.exec(html)) !== null) {
    const item = create("div", "joe_card__list-item");
    item.innerHTML = matched[1].trim().replace(/^(<br\s*\/?>)|(<br\s*\/?>)$/gi, "").trim();
    list.appendChild(item);
  }
  const wrap = create("span", "_content");
  wrap.style.display = "block";
  wrap.appendChild(list);
  this.replaceChildren(wrap);
});

defineJoeComponent("joe-timeline", function () {
  const html = holderHtml(contentHolder(this));
  const timeline = create("div", "joe_timeline");
  const reg = /\{timeline-item([^}]*)\}([\s\S]*?)\{\/timeline-item\}/g;
  let matched;
  while ((matched = reg.exec(html)) !== null) {
    const attrs = matched[1] || "";
    const content = matched[2].trim().replace(/^(<br\s*\/?>)|(<br\s*\/?>)$/gi, "").trim();
    const item = create("div", "joe_timeline__item");
    item.appendChild(create("div", "joe_timeline__item-tail"));
    const circle = create("div", "joe_timeline__item-circle");
    const colorMatch = /color\s*=\s*["']?([^"'\s]+)/i.exec(attrs);
    circle.style.borderColor = safeColor(colorMatch && colorMatch[1], "#19be6b");
    item.appendChild(circle);
    const body = create("div", "joe_timeline__item-content");
    body.innerHTML = content;
    item.appendChild(body);
    timeline.appendChild(item);
  }
  const wrap = create("span", "_content");
  wrap.style.display = "block";
  wrap.appendChild(timeline);
  this.replaceChildren(wrap);
});

defineJoeComponent("joe-collapse", function () {
  const html = holderHtml(contentHolder(this));
  const box = create("div", "joe_collapse");
  const reg = /\{collapse-item([^}]*)\}([\s\S]*?)\{\/collapse-item\}/g;
  let matched;
  while ((matched = reg.exec(html)) !== null) {
    const attrs = matched[1] || "";
    const labelMatch = /label\s*=\s*["']([^"']*)["']/i.exec(attrs);
    const label = labelMatch ? labelMatch[1] : "折叠标题";
    const opened = /(^|\s)open(\s|$)/i.test(attrs);
    const contentHtml = matched[2].trim().replace(/^(<br\s*\/?>)|(<br\s*\/?>)$/gi, "").trim();

    const item = create("div", "joe_collapse__item");
    const head = create("div", "joe_collapse__item-head");
    head.appendChild(create("div", "joe_collapse__item-head--label", label));
    const icon = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    icon.setAttribute("class", "joe_collapse__item-head--icon");
    icon.setAttribute("viewBox", "0 0 24 24");
    icon.setAttribute("width", "22");
    icon.setAttribute("height", "22");
    const path = document.createElementNS("http://www.w3.org/2000/svg", "path");
    path.setAttribute("d", "M7.406 7.828L12 12.422l4.594-4.594L18 9.234l-6 6-6-6z");
    icon.appendChild(path);
    head.appendChild(icon);

    const wrapper = create("div", "joe_collapse__item-wrapper");
    const content = create("div", "joe_collapse__item-wrapper--content");
    content.innerHTML = contentHtml;
    wrapper.appendChild(content);

    item.appendChild(head);
    item.appendChild(wrapper);
    if (opened) {
      item.classList.add("active");
      wrapper.style.maxHeight = "none";
    }
    head.addEventListener("click", () => {
      wrapper.style.maxHeight = content.offsetHeight + "px";
      setTimeout(() => {
        const active = item.classList.toggle("active");
        wrapper.style.maxHeight = active ? content.offsetHeight + "px" : "0";
      }, 30);
    });
    box.appendChild(item);
  }
  const wrap = create("span", "_content");
  wrap.style.display = "block";
  wrap.appendChild(box);
  this.replaceChildren(wrap);
});

defineJoeComponent("joe-tabs", function () {
  const html = holderHtml(contentHolder(this));
  const box = create("div", "joe_tabs");
  const head = create("div", "joe_tabs__head");
  const body = create("div", "joe_tabs__body");
  const panes = [];
  const reg = /\{tabs-pane([^}]*)\}([\s\S]*?)\{\/tabs-pane\}/g;
  let matched;
  while ((matched = reg.exec(html)) !== null) {
    const label = (matched[1] || "").trim();
    const contentHtml = matched[2].trim().replace(/^(<br\s*\/?>)|(<br\s*\/?>)$/gi, "").trim();
    const nav = create("div", "joe_tabs__head-item", label);
    const pane = create("div", "joe_tabs__body-item");
    pane.style.display = "none";
    pane.innerHTML = contentHtml;
    head.appendChild(nav);
    body.appendChild(pane);
    panes.push({nav, pane});
  }
  panes.forEach(({nav, pane}, index) => {
    nav.addEventListener("click", () => {
      panes.forEach((entry) => {
        entry.nav.classList.remove("active");
        entry.pane.style.display = "none";
      });
      nav.classList.add("active");
      pane.style.display = "block";
    });
    if (index === 0) {
      nav.classList.add("active");
      pane.style.display = "block";
    }
  });
  box.appendChild(head);
  box.appendChild(body);
  const wrap = create("span", "_content");
  wrap.style.display = "block";
  wrap.appendChild(box);
  this.replaceChildren(wrap);
});

defineJoeComponent("joe-gird", function () {
  const column = parseInt(this.getAttribute("column"), 10);
  const gap = parseInt(this.getAttribute("gap"), 10);
  const html = holderHtml(contentHolder(this));
  const grid = create("div", "joe_gird");
  grid.style.gap = (isNaN(gap) || gap <= 0 ? 15 : gap) + "px";
  grid.style.gridTemplateColumns = "repeat(" + (isNaN(column) || column <= 0 ? 3 : column) + ", 1fr)";
  const reg = /\{gird-item\}([\s\S]*?)\{\/gird-item\}/g;
  let matched;
  while ((matched = reg.exec(html)) !== null) {
    const item = create("div", "joe_gird__item");
    item.innerHTML = matched[1].trim().replace(/^(<br\s*\/?>)|(<br\s*\/?>)$/gi, "").trim();
    grid.appendChild(item);
  }
  const wrap = create("span", "_content");
  wrap.style.display = "block";
  wrap.appendChild(grid);
  this.replaceChildren(wrap);
});

defineJoeComponent("joe-lamp", function () {
  this.replaceChildren(create("span", "joe_lamp"));
});

/** 评论后可见：滚动到评论区（站点没有 joe 的 .joe_comment，改用 .post-comments） */
function renderReadLimited(host) {
  const box = create("div", "joe_read_limited");
  const paragraph = create("p");
  const icon = create("i", "fa fa-lock");
  icon.style.color = "#f5840d";
  paragraph.appendChild(icon);
  paragraph.appendChild(document.createTextNode("\u00a0此处内容仅 "));
  const button = create("span", "joe_read_limited__button", "评论");
  paragraph.appendChild(button);
  paragraph.appendChild(document.createTextNode(" 后可见"));
  box.appendChild(paragraph);
  host.replaceChildren(box);
  button.addEventListener("click", (event) => {
    event.stopPropagation();
    const target = document.querySelector(".post-comments") || document.querySelector(".joe_comment");
    if (target) {
      target.scrollIntoView({behavior: "smooth", block: "start"});
    }
  });
}

defineJoeComponent("joe-read-limited", function () {
  renderReadLimited(this);
});

defineJoeComponent("joe-hide", function () {
  renderReadLimited(this);
});

defineJoeComponent("joe-raw-content", function () {
  const raw = this.querySelector("#_raw");
  if (!raw) {
    return;
  }
  if (!this.shadowRoot) {
    const root = this.attachShadow({mode: "open"});
    root.appendChild(raw);
  }
});
