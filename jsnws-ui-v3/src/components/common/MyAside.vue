<template>
  <div class="myAside-root">
    <div class="myAside-container">
      <!-- 网站信息 -->
      <div v-if="!$common.mobile()" class="card-content1 shadow-box background-opacity">
        <div class="card-banner"></div>
        <el-avatar class="user-avatar" :size="90" :src="webInfo.avatar || '/images/default-avatar.jpg'"></el-avatar>
        <div class="web-name">{{ownerName}}</div>
        <div class="web-desc">{{ webInfo.description || webInfo.footer }}</div>
        <div class="web-info">
          <div class="blog-info-box">
            <span>文章</span>
            <span class="blog-info-num">{{ $store.getters.articleTotal }}</span>
          </div>
          <div class="blog-info-box">
            <span>分类</span>
            <span class="blog-info-num">{{ sortInfo.length }}</span>
          </div>
          <div class="blog-info-box">
            <span>访问量</span>
            <span class="blog-info-num">{{ webInfo.historyAllCount }}</span>
          </div>
        </div>
        <div class="web-social" v-if="hasSocial">
          <a v-if="webInfo.wechat" href="javascript:void(0)" title="微信" @click="wechatDialog = true">
            <i class="fa fa-weixin"></i>
          </a>
          <a v-if="webInfo.qq" :href="webInfo.qq" target="_blank" title="QQ">
            <i class="fa fa-qq"></i>
          </a>
          <a v-if="webInfo.github" :href="webInfo.github" target="_blank" title="GitHub">
            <i class="fa fa-github"></i>
          </a>
          <a v-if="webInfo.email" :href="'mailto:' + webInfo.email" title="邮箱">
            <i class="fa fa-envelope-o"></i>
          </a>
          <a v-if="webInfo.bilibili" :href="webInfo.bilibili" target="_blank" title="Bilibili">
            <!-- viewBox 裁到图形实际边界（x 3~21、y 3~20），与企鹅同为 17px 视觉尺寸 -->
            <svg viewBox="3 3 18 17" width="17" height="17" fill="currentColor" style="vertical-align: -2px">
              <path d="M7.2 3.2 9 5h6l1.8-1.8 1.4 1.4L17 6h1.5A2.5 2.5 0 0 1 21 8.5v9A2.5 2.5 0 0 1 18.5 20h-13A2.5 2.5 0 0 1 3 17.5v-9A2.5 2.5 0 0 1 5.5 6H7L5.8 4.6l1.4-1.4zM5.5 8a.5.5 0 0 0-.5.5v9c0 .28.22.5.5.5h13a.5.5 0 0 0 .5-.5v-9a.5.5 0 0 0-.5-.5h-13z"/>
              <circle cx="9" cy="12" r="1.2"/>
              <circle cx="15" cy="12" r="1.2"/>
            </svg>
          </a>
        </div>
      </div>

      <!-- 搜索 -->
      <div style="padding: 15px;border-radius: 10px;animation: hideToShow 1s ease-in-out"
           class="shadow-box background-opacity wow">
        <div style="color: var(--fontColor);font-size: 20px;font-weight: bold;margin-bottom: 10px">
          搜索
        </div>
        <div style="display: flex">
          <input class="ais-SearchBox-input" type="text"
                 v-model="articleSearch"
                 placeholder="搜索文章" maxlength="32">
          <div class="ais-SearchBox-submit" @click="selectArticle()">
            <svg style="margin-top: 3.5px;margin-left: 18px" viewBox="0 0 1024 1024" width="20" height="20">
              <path
                d="M51.2 508.8c0 256.8 208 464.8 464.8 464.8s464.8-208 464.8-464.8-208-464.8-464.8-464.8-464.8 208-464.8 464.8z"
                fill="#1f1f1f"></path>
              <path
                d="M772.8 718.4c48-58.4 76.8-132.8 76.8-213.6 0-186.4-151.2-337.6-337.6-337.6-186.4 0-337.6 151.2-337.6 337.6 0 186.4 151.2 337.6 337.6 337.6 81.6 0 156-28.8 213.6-76.8L856 896l47.2-47.2-130.4-130.4zM512 776c-149.6 0-270.4-121.6-270.4-271.2S363.2 233.6 512 233.6c149.6 0 271.2 121.6 271.2 271.2C782.4 654.4 660.8 776 512 776z"
                fill="#FFFFFF"></path>
            </svg>
          </div>
        </div>
      </div>

      <!-- 天气 -->
      <div v-if="weather" class="weather-card shadow-box background-opacity wow">
        <div class="weather-head">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.6"
               stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M7 18a4 4 0 0 1-.6-7.96 5.5 5.5 0 0 1 10.68-1.3A3.75 3.75 0 0 1 17.5 18z"/>
          </svg>
          <span>天气预报</span>
        </div>
        <div class="weather-temp">{{weather.temp}}°</div>
        <div class="weather-foot">
          <span>{{weather.city}}{{weather.desc ? '，' + weather.desc : ''}}</span>
          <span v-if="weather.aqi !== null">AQI: {{weather.aqi}}（{{weather.aqiLevel}}）</span>
        </div>
      </div>

      <div class="aside-sticky">
      <!-- 音乐 -->
      <div v-if="!$common.isEmpty(musicList)" class="music-card shadow-box background-opacity wow">
        <div class="music-title">
          <span class="music-title-bar"></span>
          <span>音乐</span>
        </div>
        <div class="music-main">
          <div class="music-cover" :class="{playing: isPlaying}">
            <img :src="currentMusic.cover" :alt="currentMusic.name">
          </div>
          <div class="music-info">
            <div class="music-name-row">
              <div class="music-name">{{currentMusic.name}}</div>
              <div v-if="!$common.isEmpty(lyrics)" class="music-lyrics-btn" :class="{active: lyricsVisible}"
                   @click="toggleLyrics" :title="lyricsVisible ? '收起歌词' : '查看歌词'">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.8"
                     stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                  <rect x="3.5" y="5.5" width="17" height="13" rx="3"/>
                  <path d="M7.5 10h9M7.5 14h5"/>
                </svg>
              </div>
            </div>
            <div class="music-artist">{{currentMusic.artist}}</div>
            <div class="music-time-row">
              <span class="music-time">{{formatTime(currentTime)}} / {{formatTime(duration)}}</span>
              <div class="music-volume-icon" @click="toggleMute" :title="muted ? '取消静音' : '静音'">
                <svg viewBox="0 0 24 24" width="14" height="14" aria-hidden="true">
                  <path d="M4 9.5h3L11.5 6v12L7 14.5H4z" fill="currentColor"/>
                  <path v-if="!muted" d="M14.6 9.2a4 4 0 0 1 0 5.6" fill="none" stroke="currentColor"
                        stroke-width="1.6" stroke-linecap="round"/>
                  <path v-else d="M14.5 9.5l3.5 5M18 9.5l-3.5 5" fill="none" stroke="currentColor"
                        stroke-width="1.6" stroke-linecap="round"/>
                </svg>
              </div>
              <div class="music-volume-bar" @click="setVolume($event)">
                <div class="music-volume-fill" :style="{width: (muted ? 0 : volume * 100) + '%'}"></div>
              </div>
            </div>
          </div>
        </div>
        <div class="music-progress" @click="seek($event)">
          <div class="music-progress-fill" :style="{width: progressPercent + '%'}"></div>
          <div v-if="duration > 0" class="music-progress-dot" :style="{left: progressPercent + '%'}"></div>
        </div>
        <div class="music-controls">
          <div class="music-btn" :class="{active: playMode === 'single'}" @click="toggleMode"
               :title="playMode === 'single' ? '单曲循环' : '列表循环'">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8"
                 stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M4 9V7.5A2.5 2.5 0 0 1 6.5 5H19"/>
              <path d="M16.5 2.5L19 5l-2.5 2.5"/>
              <path d="M20 15v1.5A2.5 2.5 0 0 1 17.5 19H5"/>
              <path d="M7.5 21.5L5 19l2.5-2.5"/>
            </svg>
          </div>
          <div class="music-btn" @click="prevMusic" title="上一首">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor" aria-hidden="true">
              <rect x="5.5" y="5.5" width="2.4" height="13" rx="1"/>
              <path d="M19 5.5v13L9 12z"/>
            </svg>
          </div>
          <div class="music-play" @click="togglePlay" :title="isPlaying ? '暂停' : '播放'">
            <svg v-if="!isPlaying" viewBox="0 0 24 24" width="16" height="16" fill="currentColor" aria-hidden="true">
              <path d="M9 6.2v11.6L19 12z"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" width="16" height="16" fill="currentColor" aria-hidden="true">
              <rect x="7.4" y="6" width="3.2" height="12" rx="1.2"/>
              <rect x="13.4" y="6" width="3.2" height="12" rx="1.2"/>
            </svg>
          </div>
          <div class="music-btn" @click="nextMusic" title="下一首">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor" aria-hidden="true">
              <rect x="16.1" y="5.5" width="2.4" height="13" rx="1"/>
              <path d="M5 5.5v13L15 12z"/>
            </svg>
          </div>
          <div class="music-list-btn" :class="{active: playlistVisible}" @click="playlistVisible = !playlistVisible"
               title="播放列表">
            <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.8"
                 stroke-linecap="round" aria-hidden="true">
              <path d="M4 7h10M4 12h10M4 17h6"/>
              <circle cx="17.5" cy="16.5" r="2.3"/>
              <path d="M19.8 16.5V9l-2 .6"/>
            </svg>
            <span class="music-list-dot"></span>
          </div>
        </div>
        <div v-if="lyricsVisible" ref="lyrics" class="music-lyrics">
          <div v-for="(lyric, index) in lyrics"
               :key="index"
               class="music-lyric"
               :class="{active: index === activeLyricIndex,
                        near: index !== activeLyricIndex && (index === activeLyricIndex - 1 || index === activeLyricIndex + 1)}">
            {{lyric.text}}
          </div>
        </div>
        <div v-if="playlistVisible" class="music-playlist">
          <div v-for="(music, index) in musicList"
               :key="index"
               class="music-playlist-item"
               :class="{active: index === currentIndex}"
               @click="selectMusic(index)">
            <span class="music-playlist-name">{{music.name}}</span>
            <span class="music-playlist-artist">{{music.artist}}</span>
          </div>
        </div>
        <audio ref="audio"
               :src="currentMusic.url"
               preload="metadata"
               style="display: none"
               @timeupdate="onTimeUpdate"
               @loadedmetadata="onLoadedMetadata"
               @durationchange="onLoadedMetadata"
               @play="isPlaying = true"
               @pause="isPlaying = false"
               @ended="onEnded"></audio>
      </div>

      <!-- 日历 -->
      <div class="calendar-card shadow-box background-opacity wow">
        <div class="calendar-head">
          <div class="calendar-nav" @click="changeMonth(-1)">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M15 5l-7 7 7 7"/>
            </svg>
          </div>
          <div class="calendar-title">{{calendarYear}}年{{calendarMonth}}月</div>
          <div class="calendar-nav" @click="changeMonth(1)">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M9 5l7 7-7 7"/>
            </svg>
          </div>
        </div>
        <div class="calendar-week">
          <span v-for="(week, index) in weekNames" :key="index">{{week}}</span>
        </div>
        <div class="calendar-grid">
          <div v-for="(day, index) in calendarDays"
               :key="index"
               class="calendar-day"
               :class="{ 'is-empty': !day.date, 'is-today': day.isToday }">
            {{day.date}}
          </div>
        </div>
      </div>

      <!-- 推荐文章 -->
      <div v-if="!$common.isEmpty(recommendArticles)"
           style="padding: 25px;border-radius: 10px;animation: hideToShow 1s ease-in-out"
           class="shadow-box background-opacity wow">
        <div class="card-content2-title">
          <span>🔥推荐文章</span>
        </div>
        <div v-for="(article, index) in recommendArticles"
             :key="index"
             @click="$router.push({path: `/article/${article.id}`})">
          <div class="aside-post-detail">
            <div class="aside-post-image">
              <el-image lazy class="my-el-image" :src="article.articleCover" fit="cover">
                <template #error>
                  <div class="image-slot">
                    <div class="error-aside-image">
                      {{article.username}}
                    </div>
                  </div>
                </template>
              </el-image>
              <div class="hasVideo transformCenter" v-if="article.hasVideo">
                <svg viewBox="0 0 1024 1024" width="30" height="30">
                  <path
                    d="M514 114.3c-219.9 0-398.9 178.9-398.9 398.9 0.1 219.9 179 398.8 398.9 398.8 219.9 0 398.8-178.9 398.8-398.8S733.9 114.3 514 114.3z m173 421.9L437.1 680.5c-17.7 10.2-39.8-2.6-39.8-23V368.9c0-20.4 22.1-33.2 39.8-23L687 490.2c17.7 10.2 17.7 35.8 0 46z"
                    fill="#0C0C0C"></path>
                </svg>
              </div>
            </div>
            <div class="aside-post-title">
              {{ article.articleTitle }}
            </div>
          </div>
          <div class="aside-post-date">
            <el-icon style="color: var(--greyFont);vertical-align: -2px"><Calendar /></el-icon>{{ article.createTime }}
          </div>
        </div>
      </div>

      <!-- 速览 -->
      <div v-if="!$common.mobile()" class="selectSort">
        <div v-for="(sort, index) in sortInfo"
             @click="selectSort(sort)"
             :key="index"
             :style="{background: $constant.sortColor[index % $constant.sortColor.length]}"
             class="shadow-box-mini background-opacity wow"
             style="position: relative;padding: 10px 25px 15px;border-radius: 10px;animation: hideToShow 1s ease-in-out;cursor: pointer;color: var(--white)">
          <div>速览</div>
          <div class="sort-name">
            {{sort.sortName}}
          </div>
          <div style="font-weight: bold;margin-top: 15px;white-space: nowrap;text-overflow: ellipsis;overflow: hidden">
            {{sort.sortDescription}}
          </div>
        </div>
      </div>

      <!-- 分类 -->
      <div class="shadow-box background-opacity wow"
           v-if="false"
           style="padding: 25px 25px 5px;border-radius: 10px;animation: hideToShow 1s ease-in-out">
        <div class="card-content2-title">
          <el-icon class="card-content2-icon"><FolderOpened /></el-icon>
          <span>分类</span>
        </div>
        <div v-for="(sort, index) in sortInfo"
             :key="index"
             class="post-sort"
             @click="$router.push({path: '/sort', query: {sortId: sort.id}})">
          <div>
            <span v-for="(s, i) in sort.sortName.split('')" :key="i">{{ s }}</span>
          </div>
        </div>
      </div>

      <!-- 赞赏 -->
      <div class="shadow-box-mini background-opacity wow admire-box"
           v-if="!$common.isEmpty(admires) && false">
        <div style="font-weight: bold;margin-bottom: 20px">🧨赞赏名单</div>
        <div>
          <Vue3SeamlessScroll :data="admires" style="height: 200px;overflow: hidden">
            <div v-for="(item, i) in admires"
                 style="display: flex;justify-content: space-between"
                 :key="i">
              <div style="display: flex">
                <el-avatar style="margin-bottom: 10px" :size="36" :src="item.avatar"></el-avatar>
                <div style="margin-left: 10px;height: 36px;line-height: 36px;overflow: hidden;max-width: 80px">
                  {{ item.name }}
                </div>
              </div>
              <div style="height: 36px;line-height: 36px">
                {{ item.amount }}元
              </div>
            </div>
          </Vue3SeamlessScroll>
        </div>
        <div class="admire-btn" @click="showAdmire()">
          赞赏
        </div>
      </div>
      </div>
    </div>

    <!-- 微信 -->
    <el-dialog title="赞赏"
               v-model="showAdmireDialog"
               width="25%"
               :append-to-body="true"
               destroy-on-close
               center>
      <div>
        <div class="admire-image"></div>
        <div>
          <div class="admire-content">1. 感谢老铁送来的666</div>
          <div class="admire-content">2. 申请通过后会加博客交流群，不需要加群或者退群后会定期清理好友（强迫症福利）</div>
        </div>
      </div>
    </el-dialog>

    <!-- 微信二维码 -->
    <el-dialog title="微信"
               v-model="wechatDialog"
               width="300px"
               :append-to-body="true"
               destroy-on-close
               center>
      <img class="wechat-qrcode" :src="webInfo.wechat" alt="微信二维码">
    </el-dialog>
  </div>
</template>

<script>
  import { Vue3SeamlessScroll } from "vue3-seamless-scroll";

  export default {
    components: {
      Vue3SeamlessScroll
    },
    data() {
      return {
        pagination: {
          current: 1,
          size: 5,
          recommendStatus: true
        },
        recommendArticles: [],
        admires: [],
        showAdmireDialog: false,
        wechatDialog: false,
        articleSearch: "",
        weather: null,
        weekNames: ["日", "一", "二", "三", "四", "五", "六"],
        calendarYear: new Date().getFullYear(),
        calendarMonth: new Date().getMonth() + 1,
        musicData: [],
        currentIndex: 0,
        currentTime: 0,
        duration: 0,
        isPlaying: false,
        volume: 0.7,
        muted: false,
        playMode: "list",
        playlistVisible: false,
        lyricsVisible: false
      }
    },
    computed: {
      musicList() {
        if (this.musicData.length > 0) {
          return this.musicData;
        }
        return this.$constant.musicList || [];
      },
      currentMusic() {
        return this.musicList[this.currentIndex] || {};
      },
      lyrics() {
        const raw = this.currentMusic.lyrics;
        if (!raw) {
          return [];
        }
        if (Array.isArray(raw)) {
          return raw;
        }
        return String(raw).split("\n").map((line) => {
          const text = line.trim();
          const match = text.match(/^\[(\d{1,2}):(\d{1,2})(?:\.(\d{1,3}))?\]\s*(.*)$/);
          if (!match) {
            return {time: 0, text: text};
          }
          const seconds = Number(match[1]) * 60 + Number(match[2]) + Number("0." + (match[3] || "0"));
          return {time: seconds, text: match[4]};
        }).filter((item) => item.text !== "");
      },
      activeLyricIndex() {
        let index = 0;
        for (let i = 0; i < this.lyrics.length; i++) {
          if (this.currentTime >= (this.lyrics[i].time || 0)) {
            index = i;
          } else {
            break;
          }
        }
        return index;
      },
      progressPercent() {
        if (!this.duration || !isFinite(this.duration)) {
          return 0;
        }
        return Math.min(100, (this.currentTime / this.duration) * 100);
      },
      calendarDays() {
        const year = this.calendarYear;
        const month = this.calendarMonth;
        const firstWeekDay = new Date(year, month - 1, 1).getDay();
        const daysInMonth = new Date(year, month, 0).getDate();
        const now = new Date();
        const isCurrentMonth = now.getFullYear() === year && now.getMonth() + 1 === month;
        const days = [];
        for (let i = 0; i < firstWeekDay; i++) {
          days.push({ date: null, isToday: false });
        }
        for (let d = 1; d <= daysInMonth; d++) {
          days.push({ date: d, isToday: isCurrentMonth && now.getDate() === d });
        }
        while (days.length % 7 !== 0) {
          days.push({ date: null, isToday: false });
        }
        return days;
      },
      webInfo() {
        return this.$store.state.webInfo;
      },
      // 卡片上的名字是站长名称：取站长账号昵称，缺失时兜底站点名称（不回落到登录用户名）
      ownerName() {
        let boss = this.$store.state.bossInfo || {};
        if (!this.$common.isEmpty(boss.username)) {
          return boss.username;
        }
        return this.$common.isEmpty(this.webInfo.webName) ? "站长" : this.webInfo.webName;
      },
      sortInfo() {
        return this.$store.getters.navigationBar;
      },
      hasSocial() {
        let w = this.webInfo;
        return !!w && !!(w.wechat || w.qq || w.github || w.email || w.bilibili);
      }
    },
    watch: {
      activeLyricIndex() {
        this.scrollLyrics();
      }
    },
    created() {
      this.getRecommendArticles();
      this.getAdmire();
      this.getMusic();
      this.getWeather();
    },
    mounted() {
      if (this.$refs.audio) {
        this.$refs.audio.volume = this.volume;
      }
    },
    methods: {
      toggleLyrics() {
        this.lyricsVisible = !this.lyricsVisible;
        if (this.lyricsVisible) {
          this.scrollLyrics();
        }
      },
      scrollLyrics() {
        this.$nextTick(() => {
          const panel = this.$refs.lyrics;
          if (!panel) {
            return;
          }
          const active = panel.querySelector(".music-lyric.active");
          if (!active) {
            return;
          }
          panel.scrollTop = active.offsetTop - panel.clientHeight / 2 + active.clientHeight / 2;
        });
      },
      togglePlay() {
        const audio = this.$refs.audio;
        if (!audio) {
          return;
        }
        if (audio.paused) {
          audio.play().catch(() => {
          });
        } else {
          audio.pause();
        }
      },
      onTimeUpdate() {
        const audio = this.$refs.audio;
        if (audio) {
          this.currentTime = audio.currentTime;
        }
      },
      onLoadedMetadata() {
        const audio = this.$refs.audio;
        this.duration = audio && isFinite(audio.duration) ? audio.duration : 0;
      },
      seek(event) {
        const audio = this.$refs.audio;
        if (!audio || !this.duration) {
          return;
        }
        const rect = event.currentTarget.getBoundingClientRect();
        const ratio = Math.min(1, Math.max(0, (event.clientX - rect.left) / rect.width));
        audio.currentTime = this.duration * ratio;
        this.currentTime = audio.currentTime;
      },
      setVolume(event) {
        const rect = event.currentTarget.getBoundingClientRect();
        const ratio = Math.min(1, Math.max(0, (event.clientX - rect.left) / rect.width));
        this.volume = ratio;
        this.muted = false;
        const audio = this.$refs.audio;
        if (audio) {
          audio.volume = ratio;
          audio.muted = false;
        }
      },
      toggleMute() {
        const audio = this.$refs.audio;
        if (!audio) {
          return;
        }
        this.muted = !this.muted;
        audio.muted = this.muted;
      },
      toggleMode() {
        this.playMode = this.playMode === "single" ? "list" : "single";
      },
      onEnded() {
        if (this.playMode === "single") {
          const audio = this.$refs.audio;
          if (audio) {
            audio.currentTime = 0;
            audio.play().catch(() => {
            });
          }
          return;
        }
        this.nextMusic(true);
      },
      nextMusic(auto) {
        if (this.$common.isEmpty(this.musicList)) {
          return;
        }
        this.selectMusic((this.currentIndex + 1) % this.musicList.length, auto === true);
      },
      prevMusic() {
        if (this.$common.isEmpty(this.musicList)) {
          return;
        }
        this.selectMusic((this.currentIndex - 1 + this.musicList.length) % this.musicList.length, this.isPlaying);
      },
      selectMusic(index, autoPlay) {
        const shouldPlay = autoPlay === true || this.isPlaying;
        this.currentIndex = index;
        this.currentTime = 0;
        this.duration = 0;
        this.$nextTick(() => {
          const audio = this.$refs.audio;
          if (!audio) {
            return;
          }
          audio.load();
          if (shouldPlay) {
            audio.play().catch(() => {
            });
          }
          if (this.lyricsVisible) {
            this.scrollLyrics();
          }
        });
      },
      formatTime(seconds) {
        if (!seconds || !isFinite(seconds)) {
          return "0:00";
        }
        const m = Math.floor(seconds / 60);
        const s = Math.floor(seconds % 60);
        return m + ":" + (s < 10 ? "0" + s : s);
      },
      changeMonth(step) {
        let year = this.calendarYear;
        let month = this.calendarMonth + step;
        if (month < 1) {
          month = 12;
          year = year - 1;
        } else if (month > 12) {
          month = 1;
          year = year + 1;
        }
        this.calendarYear = year;
        this.calendarMonth = month;
      },
      selectSort(sort) {
        this.$emit("selectSort", sort);
      },
      selectArticle() {
        this.$emit("selectArticle", this.articleSearch);
      },
      showAdmire() {
        if (this.$common.isEmpty(this.$store.state.currentUser)) {
          this.$message({
            message: "请先登录！",
            type: "error"
          });
          return;
        }

        this.showAdmireDialog = true;
      },
      getAdmire() {
        this.$http.get(this.$constant.baseURL + "/webInfo/getAdmire")
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.admires = res.data;
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      getMusic() {
        this.$http.get(this.$constant.baseURL + "/webInfo/listMusic")
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.musicData = res.data;
            }
          })
          .catch(() => {
          });
      },
      getWeather() {
        const city = this.$constant.weatherCity;
        fetch("https://geocoding-api.open-meteo.com/v1/search?name=" + encodeURIComponent(city) + "&count=1&language=zh&format=json")
          .then((res) => res.json())
          .then((geo) => {
            const place = geo.results && geo.results[0];
            if (!place) {
              return;
            }
            const location = "latitude=" + place.latitude + "&longitude=" + place.longitude;
            const getJson = (url) => fetch(url)
              .then((res) => res.json())
              .catch(() => null);
            Promise.all([
              getJson("https://api.open-meteo.com/v1/forecast?" + location + "&current=temperature_2m,weather_code&timezone=auto"),
              getJson("https://air-quality-api.open-meteo.com/v1/air-quality?" + location + "&current=us_aqi")
            ]).then(([forecast, air]) => {
              if (!forecast) {
                return;
              }
              const current = forecast.current || {};
              const aqi = air && air.current ? air.current.us_aqi : null;
              this.weather = {
                city: place.name || city,
                temp: Math.round(current.temperature_2m),
                desc: this.weatherText(current.weather_code),
                aqi: aqi === null || aqi === undefined ? null : Math.round(aqi),
                aqiLevel: this.aqiLevel(aqi)
              };
            }).catch(() => {
            });
          }).catch(() => {
          });
      },
      weatherText(code) {
        const text = {
          0: "晴", 1: "晴间多云", 2: "多云", 3: "阴",
          45: "雾", 48: "雾",
          51: "毛毛雨", 53: "毛毛雨", 55: "毛毛雨",
          56: "冻毛毛雨", 57: "冻毛毛雨",
          61: "小雨", 63: "中雨", 65: "大雨",
          66: "冻雨", 67: "冻雨",
          71: "小雪", 73: "中雪", 75: "大雪", 77: "米雪",
          80: "小阵雨", 81: "阵雨", 82: "强阵雨",
          85: "小阵雪", 86: "强阵雪",
          95: "雷阵雨", 96: "雷阵雨伴冰雹", 99: "雷阵雨伴冰雹"
        };
        return text[code] || "";
      },
      aqiLevel(aqi) {
        if (aqi === null || aqi === undefined) {
          return "";
        }
        if (aqi <= 50) return "优";
        if (aqi <= 100) return "良";
        if (aqi <= 150) return "轻度污染";
        if (aqi <= 200) return "中度污染";
        if (aqi <= 300) return "重度污染";
        return "严重污染";
      },
      getRecommendArticles() {
        this.$http.post(this.$constant.baseURL + "/article/listArticle", this.pagination)
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.recommendArticles = res.data.records;
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      }
    }
  }
</script>

<style scoped>

  .myAside-root {
    height: 100%;
  }

  .myAside-container {
    min-height: 100%;
  }

  .myAside-container > div:not(:last-child) {
    margin-bottom: 30px;
  }

  /* 音乐及以下的卡片：滑到音乐卡片后固定在视口，右侧继续滚动 */
  .aside-sticky {
    position: sticky;
    top: 70px;
  }

  .aside-sticky > div:not(:last-child) {
    margin-bottom: 30px;
  }

  .selectSort > div:not(:last-child) {
    margin-bottom: 30px;
  }

  .card-content1 {
    background: var(--cardBg);
    display: flex;
    flex-direction: column;
    align-items: center;
    border-radius: 10px;
    position: relative;
    overflow: hidden;
  }

  .card-banner {
    width: 100%;
    height: 110px;
    background: linear-gradient(to bottom, rgba(255, 255, 255, 0) 55%, var(--cardBg) 100%),
                url('/images/site-card-bg-sakura.png') center 35% / cover no-repeat;
  }

  .user-avatar {
    margin-top: -45px;
    border: 4px solid var(--cardBg);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
    z-index: 1;
  }

  .web-name {
    font-size: 20px;
    font-weight: 600;
    margin: 12px 0 6px;
    color: var(--fontColor);
  }

  .web-desc {
    max-width: 88%;
    font-size: 13px;
    color: var(--greyFont);
    text-align: center;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .web-info {
    width: 88%;
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    gap: 8px;
    margin: 16px 0 12px;
  }

  /* 三个统计项（文章 / 分类 / 访问量）各自加描边：与站内浅色边框同一套语言 */
  .blog-info-box {
    flex: 1 1 0;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 8px 4px 6px;
    font-size: 12px;
    color: var(--greyFont);
    /* 1px 浅灰描边 + 10px 圆角，无外发光/阴影；边框宽度恒定，尺寸不跳 */
    border: 1px solid var(--statBorder);
    border-radius: 10px;
    transition: border-color 0.18s ease, background-color 0.18s ease;
  }

  /* 悬停：边框由实线变虚线（线宽/圆角/颜色不变，border-style 不可动画，故只过渡颜色） */
  .blog-info-box:hover {
    border-style: dashed;
  }

  @media (prefers-reduced-motion: reduce) {
    .blog-info-box {
      transition: none;
    }
  }

  .blog-info-num {
    margin-top: 6px;
    font-size: 16px;
    font-weight: 600;
    color: var(--fontColor);
  }

  .web-social {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    font-size: 17px;
    margin-bottom: 18px;
  }

  .web-social a {
    display: flex;
    align-items: center;
    justify-content: center;
    /* 统一成企鹅图标的实际渲染框：17×17，图标在同一方形框内居中，基线一致 */
    width: 17px;
    height: 17px;
    margin: 0 9px;
    color: var(--greyFont);
    text-decoration: none;
    transition: color 0.3s;
  }

  /* GitHub 字形本身比企鹅窄（17px 字号下墨迹仅 14.5px），放大到与企鹅等宽 */
  .web-social .fa-github {
    font-size: 20px;
    line-height: 17px;
  }

  /* bilibili 是内联 SVG：元素尺寸与企鹅一致，靠 viewBox 裁到图形边界，墨迹铺满 17px */
  .web-social svg {
    width: 17px;
    height: 17px;
  }

  .web-social a:hover {
    color: var(--themeBackground);
  }

  .wechat-qrcode {
    display: block;
    width: 100%;
    max-width: 220px;
    margin: 0 auto;
  }

  .weather-card {
    padding: 20px;
    border-radius: 10px;
    animation: hideToShow 1s ease-in-out;
  }

  .weather-head {
    display: flex;
    align-items: center;
    font-size: 13px;
    color: var(--maxGreyFont);
  }

  .weather-head svg {
    flex-shrink: 0;
    margin-right: 6px;
    color: #8bb9e6;
  }

  .weather-temp {
    margin-top: 10px;
    font-size: 44px;
    font-weight: 300;
    line-height: 1.1;
    letter-spacing: 1px;
    color: var(--fontColor);
  }

  .weather-foot {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 6px;
    font-size: 12px;
    color: var(--maxGreyFont);
  }

  .music-card {
    padding: 18px 18px 20px;
    border-radius: 10px;
    animation: hideToShow 1s ease-in-out;
  }

  .music-title {
    display: flex;
    flex-direction: row;
    align-items: center;
    margin-bottom: 14px;
    font-size: 18px;
    font-weight: 700;
    color: var(--fontColor);
  }

  .music-title-bar {
    width: 3px;
    height: 16px;
    border-radius: 2px;
    background: #3b82f6;
    margin-right: 8px;
  }

  .music-main {
    display: flex;
    flex-direction: row;
    align-items: center;
  }

  .music-cover {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    overflow: hidden;
    flex-shrink: 0;
    background: var(--blueSoftBg);
    box-shadow: 0 3px 12px rgba(59, 130, 246, 0.25);
    animation: musicSpin 12s linear infinite;
    animation-play-state: paused;
  }

  .music-cover.playing {
    animation-play-state: running;
  }

  @keyframes musicSpin {
    from {
      transform: rotate(0deg);
    }
    to {
      transform: rotate(360deg);
    }
  }

  .music-cover img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .music-info {
    flex: 1;
    min-width: 0;
    margin-left: 12px;
  }

  .music-name-row {
    display: flex;
    flex-direction: row;
    align-items: center;
  }

  .music-name {
    flex: 1;
    min-width: 0;
    font-size: 15px;
    font-weight: 700;
    color: var(--fontColor);
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .music-lyrics-btn {
    width: 22px;
    height: 22px;
    margin-left: 6px;
    flex-shrink: 0;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #3b82f6;
    background: var(--blueSoftBg);
    cursor: pointer;
    transition: all 0.3s;
  }

  .music-lyrics-btn:hover {
    background: var(--blueSoftHoverBg);
  }

  .music-lyrics-btn.active {
    background: #3b82f6;
    color: var(--white);
  }

  .music-lyrics {
    position: relative;
    height: 186px;
    margin-top: 16px;
    padding: 14px 4px 16px;
    border-top: 1px solid var(--cardBorder);
    overflow-y: auto;
    text-align: center;
    scroll-behavior: smooth;
    animation: hideToShow 0.4s ease-in-out;
  }

  .music-lyrics::-webkit-scrollbar {
    width: 4px;
  }

  .music-lyrics::-webkit-scrollbar-track {
    background: transparent;
  }

  .music-lyrics::-webkit-scrollbar-thumb {
    background: var(--lightGray);
    border-radius: 2px;
  }

  .music-lyric {
    padding: 7px 2px;
    font-size: 13px;
    line-height: 1.7;
    color: var(--lyricColor);
    word-break: break-word;
    transition: color 0.3s;
  }

  .music-lyric.near {
    color: var(--lyricNearColor);
  }

  .music-lyric.active {
    color: var(--fontColor);
    font-weight: 600;
  }

  .music-artist {
    margin-top: 3px;
    font-size: 12px;
    color: var(--maxGreyFont);
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .music-time-row {
    display: flex;
    flex-direction: row;
    align-items: center;
    margin-top: 8px;
  }

  .music-time {
    flex-shrink: 0;
    font-size: 11px;
    color: var(--maxGreyFont);
  }

  .music-volume-icon {
    display: flex;
    align-items: center;
    margin-left: 10px;
    flex-shrink: 0;
    color: var(--greyFont);
    cursor: pointer;
    transition: color 0.3s;
  }

  .music-volume-icon:hover {
    color: #3b82f6;
  }

  .music-volume-bar {
    flex: 1;
    height: 4px;
    margin-left: 8px;
    border-radius: 2px;
    background: var(--trackBg);
    cursor: pointer;
    overflow: hidden;
  }

  .music-volume-fill {
    height: 100%;
    border-radius: 2px;
    background: #3b82f6;
  }

  .music-progress {
    position: relative;
    height: 4px;
    margin-top: 16px;
    border-radius: 2px;
    background: var(--trackBg);
    cursor: pointer;
  }

  .music-progress-fill {
    height: 100%;
    border-radius: 2px;
    background: #3b82f6;
  }

  .music-progress-dot {
    position: absolute;
    top: 50%;
    width: 8px;
    height: 8px;
    margin-left: -4px;
    border-radius: 50%;
    background: #3b82f6;
    transform: translateY(-50%);
    pointer-events: none;
  }

  .music-controls {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    margin-top: 16px;
  }

  .music-btn {
    width: 26px;
    height: 26px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--greyFont);
    cursor: pointer;
    transition: color 0.3s;
  }

  .music-btn:hover,
  .music-btn.active {
    color: #3b82f6;
  }

  .music-play {
    width: 42px;
    height: 42px;
    border-radius: 50%;
    border: 1.5px solid var(--blueSoftBorder);
    background: var(--blueSoftBg);
    color: #3b82f6;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s;
  }

  .music-play:hover {
    background: var(--blueSoftHoverBg);
    transform: scale(1.06);
  }

  .music-list-btn {
    position: relative;
    width: 32px;
    height: 22px;
    border-radius: 6px;
    background: var(--subtleBg);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--greyFont);
    cursor: pointer;
    transition: all 0.3s;
  }

  .music-list-btn:hover,
  .music-list-btn.active {
    color: #3b82f6;
    background: var(--blueSoftHoverBg);
  }

  .music-list-dot {
    position: absolute;
    top: 3px;
    right: 4px;
    width: 4px;
    height: 4px;
    border-radius: 50%;
    background: #f0a0a0;
  }

  .music-playlist {
    margin-top: 14px;
    padding-top: 8px;
    border-top: 1px solid var(--cardBorder);
    max-height: 132px;
    overflow-y: auto;
  }

  .music-playlist-item {
    display: flex;
    flex-direction: column;
    padding: 6px 8px;
    border-radius: 6px;
    cursor: pointer;
    transition: background 0.3s;
  }

  .music-playlist-item:hover {
    background: var(--hoverBg);
  }

  .music-playlist-name {
    font-size: 13px;
    color: var(--fontColor);
  }

  .music-playlist-item.active .music-playlist-name {
    color: #3b82f6;
    font-weight: 700;
  }

  .music-playlist-artist {
    margin-top: 2px;
    font-size: 11px;
    color: var(--maxGreyFont);
  }

  .calendar-card {
    padding: 18px 18px 20px;
    border-radius: 10px;
    animation: hideToShow 1s ease-in-out;
  }

  .calendar-head {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
  }

  .calendar-title {
    font-size: 19px;
    font-weight: 700;
    letter-spacing: 0.5px;
    color: var(--fontColor);
  }

  .calendar-nav {
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: var(--maxGreyFont);
    transition: color 0.3s;
  }

  .calendar-nav:hover {
    color: var(--fontColor);
  }

  .calendar-week {
    display: grid;
    grid-template-columns: repeat(7, minmax(0, 1fr));
    text-align: center;
    font-size: 12px;
    color: var(--maxGreyFont);
    margin-bottom: 8px;
  }

  .calendar-grid {
    display: grid;
    grid-template-columns: repeat(7, minmax(0, 1fr));
    row-gap: 6px;
  }

  .calendar-day {
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    color: var(--fontColor);
    border-radius: 6px;
    user-select: none;
  }

  .calendar-day.is-empty {
    visibility: hidden;
  }

  .calendar-day.is-today {
    border: 2px solid var(--fontColor);
    font-weight: 700;
  }

  .collection-btn {
    position: relative;
    margin-top: 12px;
    background: var(--lightGreen);
    cursor: pointer;
    width: 65%;
    height: 35px;
    border-radius: 1rem;
    text-align: center;
    line-height: 35px;
    color: var(--white);
    overflow: hidden;
    z-index: 1;
    margin-bottom: 25px;
  }

  .collection-btn::before {
    background: var(--gradualRed);
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    content: "";
    transform: scaleX(0);
    transform-origin: 0;
    transition: transform 0.5s ease-out;
    transition-timing-function: cubic-bezier(0.45, 1.64, 0.47, 0.66);
    border-radius: 1rem;
    z-index: -1;
  }

  .collection-btn:hover::before {
    transform: scaleX(1);
  }

  .card-content2-title {
    font-size: 18px;
    margin-bottom: 20px;
    color: var(--lightGreen);
    font-weight: bold;
  }

  .card-content2-icon {
    color: var(--red);
    margin-right: 5px;
    animation: scale 1s ease-in-out infinite;
  }

  .aside-post-detail {
    display: flex;
    cursor: pointer;
  }

  .aside-post-image {
    width: 40%;
    min-height: 50px;
    border-radius: 6px;
    margin-right: 8px;
    overflow: hidden;
    position: relative;
  }

  .error-aside-image {
    background: var(--themeBackground);
    color: var(--white);
    padding: 10px;
    text-align: center;
    width: 100%;
    height: 100%;
  }

  .aside-post-title {
    width: 60%;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
  }

  .aside-post-date {
    margin-top: 8px;
    margin-bottom: 20px;
    color: var(--greyFont);
    font-size: 12px;
  }

  .post-sort {
    border-radius: 1rem;
    margin-bottom: 15px;
    line-height: 30px;
    transition: all 0.3s;
  }

  .post-sort:hover {
    background: var(--themeBackground);
    padding: 2px 15px;
    cursor: pointer;
    color: var(--white);
  }

  .sort-name {
    font-weight: bold;
    font-size: 25px;
    margin-top: 15px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
  }

  .sort-name:after {
    top: 74px;
    width: 22px;
    left: 26px;
    height: 2px;
    background: var(--white);
    content: "";
    border-radius: 1px;
    position: absolute;
  }

  .admire-box {
    background: var(--springBg) center center / cover no-repeat;
    padding: 25px;
    border-radius: 10px;
    animation: hideToShow 1s ease-in-out;
  }

  .admire-btn {
    padding: 13px 15px;
    background: var(--maxLightRed);
    border-radius: 3rem;
    color: var(--white);
    width: 100px;
    user-select: none;
    cursor: pointer;
    text-align: center;
    margin: 20px auto 0;
    transition: all 1s;
  }

  .admire-btn:hover {
    transform: scale(1.2);
  }

  .admire-image {
    margin: 0 auto 10px;
    border-radius: 10px;
    height: 150px;
    width: 150px;
    background: var(--admireImage) center center / cover no-repeat;
  }

  .admire-content {
    font-size: 12px;
    color: var(--maxGreyFont);
    line-height: 1.5;
    margin: 5px;
  }

  .ais-SearchBox-input {
    padding: 0 14px;
    height: 30px;
    width: calc(100% - 50px);
    outline: 0;
    border: 2px solid var(--panelBorder);
    border-right: 0;
    border-radius: 40px 0 0 40px;
    color: var(--maxGreyFont);
    background: var(--cardBg);
  }

  .ais-SearchBox-submit {
    height: 30px;
    width: 50px;
    border: 2px solid var(--panelBorder);
    border-left: 0;
    border-radius: 0 40px 40px 0;
    background: var(--cardBg);
    cursor: pointer;
  }

  .hasVideo {
    padding: 2px 10px 0;
    background: var(--maxMaxWhiteMask);
    border-radius: 6px;
  }

</style>
