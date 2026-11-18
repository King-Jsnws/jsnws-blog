<template>
  <div class="myAside-root">
    <div class="myAside-container">
      <!-- 网站信息（统一复用站点信息卡组件） -->
      <SiteInfoCard v-if="!$common.mobile()"></SiteInfoCard>

      <!-- 搜索 -->
      <div class="side-card search-card wow">
        <div class="side-card-head">
          <span>搜索</span>
        </div>
        <div class="side-card-body">
          <div class="search-pill">
            <input class="ais-SearchBox-input" type="text"
                   v-model="articleSearch"
                   placeholder="搜索文章" maxlength="32">
            <button class="ais-SearchBox-submit" type="button" aria-label="搜索" @click="selectArticle()">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor"
                   stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <circle cx="11" cy="11" r="7"></circle>
                <path d="M20.5 20.5 16 16"></path>
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- 天气 -->
      <div v-if="weather" class="side-card weather-card wow">
        <div class="side-card-head weather-head">
          <span class="weather-head-title">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.6"
                 stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M7 18a4 4 0 0 1-.6-7.96 5.5 5.5 0 0 1 10.68-1.3A3.75 3.75 0 0 1 17.5 18z"/>
            </svg>
            <span>天气预报</span>
          </span>
        </div>
        <div class="side-card-body">
          <!-- 温度与插画同处一行，插画占固定宽高的独立槽位，后续文字排在下方正常流 -->
          <div class="weather-main-row">
            <div class="weather-temp">{{weather.temp}}°</div>
            <div class="weather-art-pos">
              <WeatherArt :desc="weather.desc"/>
            </div>
          </div>
          <div class="weather-foot">
            <span>{{weather.city}}{{weather.desc ? '，' + weather.desc : ''}}</span>
            <span v-if="weather.aqi !== null">AQI: {{weather.aqi}}（{{weather.aqiLevel}}）</span>
          </div>
        </div>
      </div>

      <!-- 日历与后续卡片同处一个吸顶块：日历在块首随视口固定，
           后续卡片在同一条文档流内排布，不会滑入吸顶区域造成叠字 -->
      <div class="aside-sticky">
      <!-- 日历：归档页右栏已有独立日历时，通过 hideCalendar 关闭本卡，避免同页出现两个日历 -->
      <div v-if="!hideCalendar" class="side-card calendar-card wow">
        <div class="side-card-head calendar-head">
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
        <div class="side-card-body">
          <div class="calendar-week">
            <span v-for="(week, index) in weekNames" :key="index">{{week}}</span>
          </div>
          <div class="calendar-grid">
            <div v-for="(day, index) in calendarDays"
                 :key="index"
                 class="calendar-day"
                 :class="{ 'is-empty': !day.date, 'is-today': day.isToday }">
              <span class="calendar-day-num">{{day.date}}</span>
              <span v-if="day.label"
                    class="calendar-day-label"
                    :class="day.labelType === 'festival' ? 'is-festival' : 'is-term'">{{day.label}}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 分类 / 标签索引：各页面侧栏共用同一套卡片；与日历同处吸顶块，视觉顺序不变 -->
      <SidebarIndexCards></SidebarIndexCards>

      <!-- 音乐 -->
      <div v-if="!$common.isEmpty(musicList)" class="side-card music-card wow">
        <div class="side-card-head">
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

      <!-- 推荐文章 -->
      <div v-if="!$common.isEmpty(recommendArticles)" class="side-card recommend-card wow">
        <div class="side-card-head">
          <span>🔥推荐文章</span>
        </div>
        <div class="side-card-body">
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
      </div>

      <!-- 速览 -->
      <div v-if="!$common.mobile()" class="selectSort">
        <div v-for="(sort, index) in sortInfo"
             @click="selectSort(sort)"
             :key="index"
             :style="{background: $constant.sortColor[index % $constant.sortColor.length]}"
             class="side-card quick-card wow">
          <div>速览</div>
          <div class="sort-name">
            {{sort.sortName}}
          </div>
          <div style="font-weight: bold;margin-top: 15px;white-space: nowrap;text-overflow: ellipsis;overflow: hidden">
            {{sort.sortDescription}}
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
  </div>
</template>

<script>
  import { Vue3SeamlessScroll } from "vue3-seamless-scroll";
  import SiteInfoCard from "./SiteInfoCard.vue";
  import SidebarIndexCards from "./SidebarIndexCards.vue";
  import WeatherArt from "./WeatherArt.vue";

  export default {
    components: {
      Vue3SeamlessScroll,
      SiteInfoCard,
      SidebarIndexCards,
      WeatherArt
    },
    props: {
      // 默认 false：六个复用页面的侧栏保持原样（含日历）；
      // 归档页右栏自带参考站日历，传 true 关闭左栏日历，避免同页重复。
      hideCalendar: {
        type: Boolean,
        default: false
      }
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
        // 当月「节气 / 节日」标注，按日号索引
        const labels = this.calendarLabels(year, month);
        const days = [];
        for (let i = 0; i < firstWeekDay; i++) {
          days.push({ date: null, isToday: false });
        }
        for (let d = 1; d <= daysInMonth; d++) {
          const label = labels[d];
          days.push({
            date: d,
            isToday: isCurrentMonth && now.getDate() === d,
            label: label ? label.text : "",
            labelType: label ? label.type : ""
          });
        }
        while (days.length % 7 !== 0) {
          days.push({ date: null, isToday: false });
        }
        return days;
      },
      sortInfo() {
        return this.$store.getters.navigationBar;
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
      // 农历换算：交给浏览器内置的中国历（ICU），不联网；闰月或解析失败时返回 null，对应日期不标注
      lunarDate(date) {
        try {
          const parts = new Intl.DateTimeFormat("zh-CN-u-ca-chinese", {
            month: "numeric",
            day: "numeric"
          }).formatToParts(date);
          const monthPart = parts.find(p => p.type === "month");
          const dayPart = parts.find(p => p.type === "day");
          // 闰月的 month 值不是纯数字（如 2bis），此处直接放弃该日期的标注，避免误标节日
          if (!monthPart || !dayPart || !/^\d+$/.test(monthPart.value)) {
            return null;
          }
          return {
            month: parseInt(monthPart.value, 10),
            day: parseInt(dayPart.value, 10)
          };
        } catch (e) {
          return null;
        }
      },
      // 生成某年某月的标注表：日号 -> { text, type }
      calendarLabels(year, month) {
        const labels = {};
        // 公历固定节日（清明由节气表返回，标明为节日）
        const solarFestivals = {"1-1": "元旦", "5-1": "劳动", "10-1": "国庆"};
        const solar = solarFestivals[month + "-1"];
        if (solar) {
          labels[1] = {text: solar, type: "festival"};
        }
        // 二十四节气：1900 年小寒起的分钟偏移推算，覆盖 1900-2100，超出范围不显示
        if (year >= 1900 && year <= 2100) {
          const termMinutes = [0, 21208, 42467, 63836, 85337, 107014, 128867, 150921, 173149, 195551, 218072, 240693,
            263343, 285989, 308563, 331033, 353350, 375494, 397447, 419210, 440795, 462224, 483532, 504758];
          const termNames = ["小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明", "谷雨", "立夏", "小满", "芒种", "夏至",
            "小暑", "大暑", "立秋", "处暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"];
          const baseUtc = Date.UTC(1900, 0, 6, 2, 5);
          [(month - 1) * 2, (month - 1) * 2 + 1].forEach((n) => {
            const time = new Date(baseUtc + 31556925974.7 * (year - 1900) + termMinutes[n] * 60000);
            if (time.getUTCMonth() + 1 !== month) {
              return;
            }
            const day = time.getUTCDate();
            // 清明既是节气也是法定节假日，按节日样式显示；同日已有公历节日则不覆盖
            labels[day] = {text: termNames[n], type: termNames[n] === "清明" ? "festival" : "term"};
          });
        }
        // 农历节日 + 除夕（除夕通过「次日为正月初一」判定，跨年月份同样有效）
        const lunarFestivals = {
          "1-1": "春节",
          "1-15": "元宵",
          "5-5": "端午",
          "7-7": "七夕",
          "8-15": "中秋",
          "9-9": "重阳"
        };
        const daysInMonth = new Date(year, month, 0).getDate();
        for (let d = 1; d <= daysInMonth; d++) {
          const lunar = this.lunarDate(new Date(year, month - 1, d));
          if (!lunar) {
            continue;
          }
          const festival = lunarFestivals[lunar.month + "-" + lunar.day];
          if (festival) {
            labels[d] = {text: festival, type: "festival"};
          }
          const nextLunar = this.lunarDate(new Date(year, month - 1, d + 1));
          if (nextLunar && nextLunar.month === 1 && nextLunar.day === 1) {
            labels[d] = {text: "除夕", type: "festival"};
          }
        }
        return labels;
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

  /* ===== 侧栏卡片统一外壳：与分类 / 标签卡（参考站 card-base）同一套 =====
     卡片底色 + 1px 描边 + 16px 圆角 + 1px 阴影 + 上 0 / 左右 0 / 下 16px 内边距，
     卡头统一 18px / 700 + 左侧 4×16 主题色竖条，内容区左右 16px */
  .side-card {
    box-sizing: border-box;
    padding: 0 0 16px;
    border: var(--ui-card-border);
    border-radius: var(--ui-card-radius);
    background: var(--ui-card-bg);
    box-shadow: var(--ui-card-shadow);
  }

  .side-card-head {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 16px 16px 8px 32px;
    font-size: 18px;
    font-weight: 700;
    line-height: 1.4;
    color: var(--fontColor);
  }

  /* 卡头左侧竖条：4px 宽、16px 高、6px 圆角，距卡片左边缘 16px */
  .side-card-head::before {
    content: "";
    position: absolute;
    top: 5.5px;
    left: -16px;
    width: 4px;
    height: 16px;
    border-radius: 6px;
    background: var(--ui-accent);
  }

  .side-card-body {
    padding: 0 16px;
  }

  .weather-card {
    position: relative;
    overflow: hidden;
    animation: hideToShow 1s ease-in-out;
  }

  /* 搜索卡：外壳由 .side-card 统一提供，这里只保留入场动画 */
  .search-card {
    animation: hideToShow 1s ease-in-out;
  }

  /* 温度与插画一行：插画占固定槽位，文字不再与插画重叠 */
  .weather-main-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-top: 10px;
  }

  .weather-art-pos {
    position: static;
    flex: 0 0 64px;
    width: 64px;
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .weather-art-pos :deep(.weather-art),
  .weather-art-pos :deep(svg) {
    width: 100%;
    height: 100%;
  }

  @media (max-width: 768px) {
    .weather-art-pos {
      flex-basis: 56px;
      width: 56px;
      height: 56px;
    }
  }

  /* 天气卡头里的「图标 + 天气预报」：字号 / 颜色 / 间距由 .side-card-head 统一 */
  .weather-head-title {
    display: inline-flex;
    align-items: center;
    min-width: 0;
  }

  .weather-head svg {
    flex-shrink: 0;
    margin-right: 6px;
    color: #8bb9e6;
  }

  .weather-temp {
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
    animation: hideToShow 1s ease-in-out;
  }

  /* 音乐卡内容与卡头对齐：左右各 16px（外壳由 .side-card 统一提供） */
  .music-main,
  .music-progress,
  .music-controls,
  .music-lyrics,
  .music-playlist {
    margin-left: 16px;
    margin-right: 16px;
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
    animation: hideToShow 1s ease-in-out;
  }

  /* 日历卡头即「月份切换行」：字号 / 字重 / 竖条 / 间距由 .side-card-head 统一 */
  .calendar-title {
    letter-spacing: 0.5px;
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
    /* 高度容纳「日号 + 小字标注」，用 border-box 保证今日描边不改变格子尺寸 */
    box-sizing: border-box;
    height: 38px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 1px;
    font-size: 14px;
    color: var(--fontColor);
    border-radius: 6px;
    user-select: none;
  }

  .calendar-day-num {
    line-height: 17px;
  }

  .calendar-day-label {
    max-width: 100%;
    font-size: 9px;
    line-height: 10px;
    letter-spacing: -0.2px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: clip;
  }

  /* 节气用次要文字色，节日用暖红强调，深浅模式各自适配 */
  .calendar-day-label.is-term {
    color: var(--maxGreyFont);
  }

  .calendar-day-label.is-festival {
    color: #d4553a;
  }

  html.dark .calendar-day-label.is-festival {
    color: #f08a68;
  }

  @media (max-width: 768px) {
    .calendar-day-label {
      font-size: 8px;
    }
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
    background: var(--ui-accent-strong);
    cursor: pointer;
    width: 65%;
    min-height: 34px;
    padding: 6px 12px;
    border: 1px solid transparent;
    border-radius: var(--ui-btn-radius);
    box-shadow: var(--ui-btn-shadow);
    text-align: center;
    line-height: 20px;
    color: var(--white);
    font-size: 14px;
    font-weight: 500;
    overflow: hidden;
    z-index: 1;
    margin-bottom: 25px;
    transition: var(--ui-btn-transition);
  }

  .collection-btn::before {
    background: var(--ui-accent-strong-hover);
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
    border-radius: var(--ui-btn-radius);
    z-index: -1;
  }

  .collection-btn:hover::before {
    transform: scaleX(1);
  }

  /* 按压瞬时缩放，与参考站 active:scale-95 一致 */
  .collection-btn:active {
    border-color: rgba(255, 255, 255, 0.55);
    border-style: dashed;
    box-shadow: var(--ui-btn-shadow-hover);
    scale: 0.95;
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

  /* 速览：底色仍按分类色（内容自带），只统一外壳的圆角 / 描边 / 阴影 / 内边距 */
  .quick-card {
    position: relative;
    padding: 16px;
    cursor: pointer;
    color: var(--white);
    animation: hideToShow 1s ease-in-out;
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
    top: 80px;
    width: 22px;
    left: 17px;
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
    padding: 8px 14px;
    min-height: 36px;
    background: var(--maxLightRed);
    border: 1px solid transparent;
    border-radius: var(--ui-btn-radius);
    box-shadow: var(--ui-btn-shadow);
    color: var(--white);
    width: 100px;
    font-size: 14px;
    font-weight: 500;
    line-height: 20px;
    user-select: none;
    cursor: pointer;
    text-align: center;
    margin: 20px auto 0;
    transition: var(--ui-btn-transition);
  }

  .admire-btn:hover {
    border-style: dashed;
    border-color: rgba(255, 255, 255, 0.55);
    box-shadow: var(--ui-btn-shadow-hover);
  }

  /* 按压瞬时缩放，与参考站 active:scale-95 一致 */
  .admire-btn:active {
    border-style: dashed;
    border-color: rgba(255, 255, 255, 0.55);
    box-shadow: var(--ui-btn-shadow-hover);
    scale: 0.95;
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

  /* 搜索条：按参考图落地的胶囊形单行输入——浅蓝 1px 描边 + 近白底 + 右侧细线放大镜，
     尺寸沿用改造前的原值（30px 高、左右 14px 内边距、20px 图标、默认正文字号），
     放大镜与输入区同处一个胶囊内，输入框只负责剩余宽度 */
  .search-pill {
    display: flex;
    align-items: center;
    gap: 10px;
    height: 30px;
    padding: 0 14px;
    border: 1px solid #e1eefc;
    border-radius: 999px;
    background: #fefeff;
  }

  /* 聚焦只换描边与底色，不加动效 */
  .search-pill:focus-within {
    border-color: #bcd9f8;
    background: #ffffff;
  }

  .ais-SearchBox-submit {
    flex: none;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    padding: 0;
    border: 0;
    background: transparent;
    color: #b2b2b3;
    cursor: pointer;
  }

  .ais-SearchBox-submit:hover {
    color: #8a8f94;
  }

  .ais-SearchBox-input {
    flex: 1;
    min-width: 0;
    height: 100%;
    padding: 0;
    border: 0;
    outline: 0;
    background: transparent;
    color: #2f343a;
  }

  .ais-SearchBox-input::placeholder {
    color: #6b7280;
  }

  html.dark .search-pill {
    border-color: rgba(255, 255, 255, 0.16);
    background: rgba(255, 255, 255, 0.04);
  }

  html.dark .search-pill:focus-within {
    border-color: rgba(99, 179, 255, 0.55);
    background: rgba(255, 255, 255, 0.06);
  }

  html.dark .ais-SearchBox-submit {
    color: rgba(255, 255, 255, 0.55);
  }

  html.dark .ais-SearchBox-submit:hover {
    color: rgba(255, 255, 255, 0.8);
  }

  html.dark .ais-SearchBox-input {
    color: #e6e6e6;
  }

  html.dark .ais-SearchBox-input::placeholder {
    color: #9aa4af;
  }

  .hasVideo {
    padding: 2px 10px 0;
    background: var(--maxMaxWhiteMask);
    border-radius: 6px;
  }

</style>
