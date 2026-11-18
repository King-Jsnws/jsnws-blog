<template>
  <section class="analytics-page" :class="{ 'is-loading': loading }" aria-labelledby="analytics-title">
    <!-- 页头 -->
    <header class="analytics-hero">
      <div class="analytics-hero__grid">
        <div class="analytics-hero__copy">
          <div class="analytics-kicker">
            <span class="analytics-kicker__signal" aria-hidden="true"></span>
            <span>PUBLIC TELEMETRY</span>
          </div>
          <h1 id="analytics-title">站点统计</h1>
          <p class="analytics-hero__lead">这里展示的是本站的整体趋势，不记录任何可识别个人身份的信息。</p>
          <div class="analytics-hero__notes">
            <span>
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path fill="currentColor" d="M6 22q-.825 0-1.412-.587T4 20V10q0-.825.588-1.412T6 8h9V6q0-1.25-.875-2.125T12 3q-1.05 0-1.838.638T9.1 5.225q-.1.35-.413.563T8 6q-.425 0-.712-.275t-.213-.65q.275-1.7 1.662-2.888T12 1q2.075 0 3.538 1.463T17 6v2h1q.825 0 1.413.588T20 10v10q0 .825-.587 1.413T18 22zm7.413-5.587Q14 15.825 14 15t-.587-1.412T12 13t-1.412.588T10 15t.588 1.413T12 17t1.413-.587"/>
              </svg>公开只读
            </span>
            <span>
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path fill="currentColor" d="M5 22q-.825 0-1.412-.587T3 20V6q0-.825.588-1.412T5 4h1V3q0-.425.288-.712T7 2t.713.288T8 3v1h8V3q0-.425.288-.712T17 2t.713.288T18 3v1h1q.825 0 1.413.588T21 6v14q0 .825-.587 1.413T19 22zm0-2h14V10H5z"/>
              </svg>按北京时间汇总
            </span>
          </div>
        </div>
        <div class="analytics-hero__meta">
          <div class="analytics-status" :data-state="loadState" role="status" aria-live="polite">
            <span class="analytics-status__dot" aria-hidden="true"></span>
            <span>{{ statusText }}</span>
          </div>
          <div class="analytics-hero__stamp">
            <span>DATA SOURCE</span>
            <strong>本站访问记录</strong>
            <small>{{ updatedText }}</small>
          </div>
        </div>
      </div>
      <div class="analytics-hero__rail" aria-hidden="true">
        <span>RZ / 2026</span>
        <span></span>
        <span>MEASURE WHAT MATTERS</span>
      </div>
    </header>

    <!-- 概览 -->
    <section class="analytics-summary" aria-label="站点概览">
      <article v-for="card in summaryCards" :key="card.label"
               class="analytics-summary-card" :class="'analytics-summary-card--' + card.variant">
        <div class="analytics-summary-card__topline">
          <span class="analytics-summary-card__label">{{ card.eyebrow }}</span>
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path v-if="card.icon === 'group'" fill="currentColor" d="M1 17.2q0-.85.438-1.562T2.6 14.55q1.55-.775 3.15-1.162T9 13t3.25.388t3.15 1.162q.725.375 1.163 1.088T17 17.2v.8q0 .825-.587 1.413T15 20H3q-.825 0-1.412-.587T1 18zM18.45 20q.275-.45.413-.962T19 18v-1q0-1.1-.612-2.113T16.65 13.15q1.275.15 2.4.513t2.1.887q.9.5 1.375 1.112T23 17v1q0 .825-.587 1.413T21 20zM6.175 10.825Q5 9.65 5 8t1.175-2.825T9 4t2.825 1.175T13 8t-1.175 2.825T9 12t-2.825-1.175m11.65 0Q16.65 12 15 12q-.275 0-.7-.062t-.7-.138q.675-.8 1.038-1.775T15 8t-.362-2.025T13.6 4.2q.35-.125.7-.163T15 4q1.65 0 2.825 1.175T19 8t-1.175 2.825"/>
            <path v-else-if="card.icon === 'visibility'" fill="currentColor" d="M15.188 14.688Q16.5 13.375 16.5 11.5t-1.312-3.187T12 7T8.813 8.313T7.5 11.5t1.313 3.188T12 16t3.188-1.312m-5.1-1.276Q9.3 12.625 9.3 11.5t.788-1.912T12 8.8t1.913.788t.787 1.912t-.787 1.913T12 14.2t-1.912-.787m-4.2 3.787q-2.763-1.8-4.363-4.75q-.125-.225-.187-.462t-.063-.488t.063-.488t.187-.462q1.6-2.95 4.363-4.75T12 4t6.113 1.8t4.362 4.75q.125.225.188.463t.062.487t-.062.488t-.188.462q-1.6 2.95-4.362 4.75T12 19t-6.113-1.8"/>
            <path v-else-if="card.icon === 'person'" fill="currentColor" d="M11 12q-1.65 0-2.825-1.175T7 8t1.175-2.825T11 4t2.825 1.175T15 8t-1.175 2.825T11 12m10.4 10.8l-2.5-2.5q-.525.3-1.125.5T16.5 21q-1.875 0-3.187-1.312T12 16.5t1.313-3.187T16.5 12t3.188 1.313T21 16.5q0 .675-.2 1.275t-.5 1.125l2.5 2.5q.275.275.275.7t-.275.7t-.7.275t-.7-.275m-3.125-4.525Q19 17.55 19 16.5t-.725-1.775T16.5 14t-1.775.725T14 16.5t.725 1.775T16.5 19t1.775-.725M5 20q-.825 0-1.412-.587T3 18v-.775q0-.85.425-1.575t1.175-1.1q1.125-.575 2.488-.987t2.987-.538q.3-.025.45.25t.025.575q-.275.625-.413 1.288T10 16.475q0 .65.113 1.313t.387 1.262q.15.35-.025.65t-.525.3z"/>
            <path v-else fill="currentColor" d="M2.7 17.625q-.3-.3-.288-.712t.288-.688l5.275-5.35Q8.55 10.3 9.4 10.3t1.425.575l2.575 2.6l5.2-5.15H17q-.425 0-.712-.288T16 7.326t.288-.712t.712-.288h4q.425 0 .713.288t.287.712v4q0 .425-.288.713t-.712.287t-.712-.287t-.288-.713v-1.6L14.825 14.9q-.575.575-1.425.575t-1.425-.575L9.4 12.325l-5.3 5.3q-.275.275-.7.275t-.7-.275"/>
          </svg>
        </div>
        <strong>{{ loading ? '加载中' : formatNumber(card.value) }}</strong>
        <span>{{ card.label }}</span>
        <small>{{ card.small }}</small>
      </article>
    </section>

    <div class="analytics-layout">
      <main class="analytics-main">
        <!-- 访问脉冲 -->
        <section class="analytics-panel analytics-traffic card-base" aria-labelledby="analytics-traffic-title">
          <header class="analytics-panel__header">
            <div>
              <p class="analytics-section-kicker">TRAFFIC / SIGNAL</p>
              <h2 id="analytics-traffic-title">访问脉冲</h2>
              <p class="analytics-panel__description">每天的浏览量与访客数 · 刷新可重播动画</p>
            </div>
            <div class="analytics-panel__controls">
              <div class="analytics-range" role="group" aria-label="选择统计时间范围">
                <button v-for="day in ranges" :key="day" type="button"
                        :aria-pressed="range === day" @click="changeRange(day)">
                  {{ day }} 天
                </button>
              </div>
              <button type="button" class="analytics-refresh" aria-label="刷新统计数据并重播动画" @click="load(true)">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path fill="currentColor" d="M12 20q-3.35 0-5.675-2.325T4 12t2.325-5.675T12 4q1.725 0 3.3.712T18 6.75V5q0-.425.288-.712T19 4t.713.288T20 5v5q0 .425-.288.713T19 11h-5q-.425 0-.712-.288T13 10t.288-.712T14 9h3.2q-.8-1.4-2.187-2.2T12 6Q9.5 6 7.75 7.75T6 12t1.75 4.25T12 18q1.7 0 3.113-.862t2.187-2.313q.2-.35.563-.487t.737-.013q.4.125.575.525t-.025.75q-1.025 2-2.925 3.2T12 20"/>
                </svg>
              </button>
            </div>
          </header>

          <div class="analytics-chart-wrap" @mousemove="onChartMove" @mouseleave="hoverIndex = -1">
            <div class="analytics-chart-labels" aria-hidden="true">
              <span>{{ hasTraffic ? chart.grid[0].label : '—' }}</span>
              <span>{{ hasTraffic ? chart.grid[1].label : '—' }}</span>
              <span>0</span>
            </div>
            <svg class="analytics-chart" :class="{ 'is-empty': !hasTraffic }"
                 viewBox="0 0 960 300" role="img" aria-label="最近 30 天的浏览量与访客趋势">
              <defs>
                <linearGradient id="analytics-area-gradient" x1="0" x2="0" y1="0" y2="1">
                  <stop offset="0%" stop-color="var(--analytics-blue)" stop-opacity="0.24"></stop>
                  <stop offset="100%" stop-color="var(--analytics-blue)" stop-opacity="0"></stop>
                </linearGradient>
              </defs>
              <g class="analytics-chart__grid" aria-hidden="true">
                <line :x1="chart.left" :x2="chart.right" y1="38" y2="38"></line>
                <line :x1="chart.left" :x2="chart.right" y1="134" y2="134"></line>
                <line :x1="chart.left" :x2="chart.right" y1="230" y2="230"></line>
              </g>
              <path class="analytics-chart__area" :d="chart.areaPath"></path>
              <path class="analytics-chart__line analytics-chart__line--views" :d="chart.pvPath"></path>
              <path class="analytics-chart__line analytics-chart__line--sessions" :d="chart.uvPath"></path>
              <line v-if="hoverPoint" class="analytics-chart__guide" :x1="hoverPoint.x" :x2="hoverPoint.x" y1="24" y2="230"></line>
              <g class="analytics-chart__points">
                <circle v-for="point in chart.points" :key="'p' + point.date"
                        class="analytics-chart__point" :cx="point.x" :cy="point.pvY" r="2.8"></circle>
              </g>
              <g class="analytics-chart__endpoints" aria-hidden="true">
                <template v-if="chart.points.length">
                  <circle class="analytics-chart__endpoint analytics-chart__endpoint--views"
                          :cx="chart.lastPoint.x" :cy="chart.lastPoint.pvY" r="4"></circle>
                  <circle class="analytics-chart__endpoint analytics-chart__endpoint--sessions"
                          :cx="chart.lastPoint.x" :cy="chart.lastPoint.uvY" r="3.4"></circle>
                </template>
              </g>
            </svg>
            <div v-if="hoverPoint" class="analytics-chart__tooltip" :style="{ left: tipLeft + '%' }">
              <strong>{{ formatMonthDay(hoverPoint.date) }}</strong>
              <span><i class="analytics-legend-dot analytics-legend-dot--blue"></i><b>{{ formatNumber(hoverPoint.pageviews) }}</b> 浏览</span>
              <span><i class="analytics-legend-dot analytics-legend-dot--gold"></i><b>{{ formatNumber(hoverPoint.visitors) }}</b> 访客</span>
            </div>
            <div class="analytics-chart__x-axis" aria-hidden="true">
              <span>{{ chart.xLabels[0] ? chart.xLabels[0].text : '—' }}</span>
              <span>{{ chart.xLabels[1] ? chart.xLabels[1].text : '—' }}</span>
              <span>{{ chart.xLabels[2] ? chart.xLabels[2].text : '今天' }}</span>
            </div>
            <div v-if="!hasTraffic" class="analytics-chart-empty">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path fill="currentColor" d="M5 20v-6h3v6zm5.5 0V9h3v11zm5.5 0V4h3v16z"/>
              </svg>
              <strong>暂时没有可展示的趋势</strong>
              <span>公开数据接口暂时没有返回这段时间的记录。</span>
            </div>
          </div>
          <div class="analytics-chart-footer">
            <div class="analytics-chart-legend">
              <span><i class="analytics-legend-dot analytics-legend-dot--blue"></i>浏览量</span>
              <span><i class="analytics-legend-dot analytics-legend-dot--gold"></i>访客</span>
            </div>
          </div>

          <!-- 本周期数据 + 页面排行 -->
          <div class="analytics-traffic__details">
            <div class="analytics-traffic__period">
              <div class="analytics-snapshot">
                <div class="analytics-snapshot__intro">
                  <strong>本周期数据</strong>
                  <span>近 30 天 · 北京时间</span>
                </div>
                <div class="analytics-snapshot__metrics">
                  <div class="analytics-snapshot__metric analytics-snapshot__metric--blue">
                    <span class="analytics-snapshot__metric-icon analytics-snapshot__metric-icon--blue">
                      <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M15.188 14.688Q16.5 13.375 16.5 11.5t-1.312-3.187T12 7T8.813 8.313T7.5 11.5t1.313 3.188T12 16t3.188-1.312m-5.1-1.276Q9.3 12.625 9.3 11.5t.788-1.912T12 8.8t1.913.788t.787 1.912t-.787 1.913T12 14.2t-1.912-.787m-4.2 3.787q-2.763-1.8-4.363-4.75q-.125-.225-.187-.462t-.063-.488t.063-.488t.187-.462q1.6-2.95 4.363-4.75T12 4t6.113 1.8t4.362 4.75q.125.225.188.463t.062.487t-.062.488t-.188.462q-1.6 2.95-4.362 4.75T12 19t-6.113-1.8"/></svg>
                    </span>
                    <div class="analytics-snapshot__metric-copy">
                      <span>浏览量</span>
                      <small v-if="changeOf(periodData.pageviews, periodData.previousPageviews)"
                             :data-direction="changeOf(periodData.pageviews, periodData.previousPageviews).up ? 'up' : 'down'">
                        {{ changeOf(periodData.pageviews, periodData.previousPageviews).text }} 较上周期
                      </small>
                    </div>
                    <strong>{{ formatNumber(periodData.pageviews) }}</strong>
                  </div>
                  <div class="analytics-snapshot__metric analytics-snapshot__metric--pink">
                    <span class="analytics-snapshot__metric-icon analytics-snapshot__metric-icon--pink">
                      <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M1 17.2q0-.85.438-1.562T2.6 14.55q1.55-.775 3.15-1.162T9 13t3.25.388t3.15 1.162q.725.375 1.163 1.088T17 17.2v.8q0 .825-.587 1.413T15 20H3q-.825 0-1.412-.587T1 18zM18.45 20q.275-.45.413-.962T19 18v-1q0-1.1-.612-2.113T16.65 13.15q1.275.15 2.4.513t2.1.887q.9.5 1.375 1.112T23 17v1q0 .825-.587 1.413T21 20zM6.175 10.825Q5 9.65 5 8t1.175-2.825T9 4t2.825 1.175T13 8t-1.175 2.825T9 12t-2.825-1.175m11.65 0Q16.65 12 15 12q-.275 0-.7-.062t-.7-.138q.675-.8 1.038-1.775T15 8t-.362-2.025T13.6 4.2q.35-.125.7-.163T15 4q1.65 0 2.825 1.175T19 8t-1.175 2.825"/></svg>
                    </span>
                    <div class="analytics-snapshot__metric-copy">
                      <span>访客数</span>
                      <small v-if="changeOf(periodData.visitors, periodData.previousVisitors)"
                             :data-direction="changeOf(periodData.visitors, periodData.previousVisitors).up ? 'up' : 'down'">
                        {{ changeOf(periodData.visitors, periodData.previousVisitors).text }} 较上周期
                      </small>
                    </div>
                    <strong>{{ formatNumber(periodData.visitors) }}</strong>
                  </div>
                  <div class="analytics-snapshot__metric analytics-snapshot__metric--mint">
                    <span class="analytics-snapshot__metric-icon analytics-snapshot__metric-icon--mint">
                      <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M5 22q-.825 0-1.412-.587T3 20V6q0-.825.588-1.412T5 4h1V3q0-.425.288-.712T7 2t.713.288T8 3v1h8V3q0-.425.288-.712T17 2t.713.288T18 3v1h1q.825 0 1.413.588T21 6v14q0 .825-.587 1.413T19 22zm0-2h14V10H5z"/></svg>
                    </span>
                    <div class="analytics-snapshot__metric-copy">
                      <span>活跃天数</span>
                      <small>有访问记录的日期</small>
                    </div>
                    <strong>{{ periodData.activeDays }}/{{ periodData.days }}</strong>
                  </div>
                  <div class="analytics-snapshot__metric analytics-snapshot__metric--gold">
                    <span class="analytics-snapshot__metric-icon analytics-snapshot__metric-icon--gold">
                      <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M2.7 17.625q-.3-.3-.288-.712t.288-.688l5.275-5.35Q8.55 10.3 9.4 10.3t1.425.575l2.575 2.6l5.2-5.15H17q-.425 0-.712-.288T16 7.326t.288-.712t.712-.288h4q.425 0 .713.288t.287.712v4q0 .425-.288.713t-.712.287t-.712-.287t-.288-.713v-1.6L14.825 14.9q-.575.575-1.425.575t-1.425-.575L9.4 12.325l-5.3 5.3q-.275.275-.7.275t-.7-.275"/></svg>
                    </span>
                    <div class="analytics-snapshot__metric-copy">
                      <span>单日峰值</span>
                      <small>最高浏览量</small>
                    </div>
                    <strong>{{ formatNumber(periodData.peak) }}</strong>
                  </div>
                </div>
              </div>
            </div>
            <div class="analytics-traffic__ranking">
              <div class="analytics-traffic__section-heading">
                <strong>大家在看什么</strong>
                <span>近 30 天 · 访客</span>
              </div>
              <ul class="analytics-ranking">
                <li v-for="(page, index) in pages.slice(0, 4)" :key="page.path" class="analytics-ranking__item">
                  <a class="analytics-ranking__link" href="#" :style="{ '--ranking-scale': pageBarWidth(page.num) / 100 }" @click.prevent="go(page.path)">
                    <i class="analytics-ranking__index">{{ pad(index + 1) }}</i>
                    <span class="analytics-ranking__copy">
                      <strong>{{ pageTitle(page.path) }}</strong>
                      <small>{{ pageSub(page.path) }}</small>
                    </span>
                    <strong>{{ formatNumber(page.num) }}</strong>
                  </a>
                </li>
                <li v-if="!pages.length" class="analytics-sources__empty">暂无页面访问记录</li>
              </ul>
            </div>
          </div>
        </section>

        <!-- 推送节奏 -->
        <section class="analytics-panel analytics-deployment card-base" aria-labelledby="analytics-deployment-title">
          <header class="analytics-panel__header analytics-deployment__header">
            <div class="analytics-deployment__heading">
              <p class="analytics-section-kicker">OPERATIONS / RELEASES</p>
              <div class="analytics-deployment__title-row">
                <h2 id="analytics-deployment-title">推送节奏</h2>
                <span class="analytics-deployment__status">未接入</span>
              </div>
              <p class="analytics-deployment__note">按 GitHub main 分支推送时间观察活动密度</p>
            </div>
            <div class="analytics-deployment__art" aria-hidden="true">
              <img src="/images/analytics/push-rhythm-character.63f0d8.webp" alt="">
              <p>持续热爱，<br>一直在更新！</p>
              <span>♥</span>
            </div>
          </header>
          <ul class="analytics-deployment__metrics">
            <li class="analytics-deployment__metric analytics-deployment__metric--blue analytics-deployment__metric--latest">
              <span class="analytics-deployment__metric-icon">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="m12 12.675l-4.3 2.6q-.25.15-.5 0t-.25-.45v-8.6q0-.4.35-.6l4.225-2.55q.225-.15.475-.15t.475.15L16.7 5.625q.35.2.35.6v8.6q0 .3-.25.45t-.5 0zM12 11l3.825-2.3L12 6.4L8.175 8.7z"/></svg>
              </span>
              <div class="analytics-deployment__metric-copy"><span>最近一次推送</span><strong>—</strong><small>暂无数据</small></div>
              <em>暂无数据</em>
            </li>
            <li class="analytics-deployment__metric analytics-deployment__metric--pink">
              <span class="analytics-deployment__metric-icon">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M5 22q-.825 0-1.412-.587T3 20V6q0-.825.588-1.412T5 4h1V3q0-.425.288-.712T7 2t.713.288T8 3v1h8V3q0-.425.288-.712T17 2t.713.288T18 3v1h1q.825 0 1.413.588T21 6v14q0 .825-.587 1.413T19 22zm0-2h14V10H5z"/></svg>
              </span>
              <div class="analytics-deployment__metric-copy"><span>最近间隔</span><strong>—</strong><small>暂无推送记录</small></div>
            </li>
            <li class="analytics-deployment__metric analytics-deployment__metric--violet">
              <span class="analytics-deployment__metric-icon">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M5 20v-6h3v6zm5.5 0V9h3v11zm5.5 0V4h3v16z"/></svg>
              </span>
              <div class="analytics-deployment__metric-copy"><span>平均节奏</span><strong>—</strong><small>暂无推送记录</small></div>
            </li>
            <li class="analytics-deployment__metric analytics-deployment__metric--pink">
              <span class="analytics-deployment__metric-icon">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M6 20q-.825 0-1.412-.587T4 18v-1q0-.825.588-1.412T6 15h12q.825 0 1.413.588T20 17v1q0 .825-.587 1.413T18 20zm5.5-7q-1.875 0-3.187-1.312T7 8.5t1.313-3.187T11.5 4t3.188 1.313T16 8.5t-1.312 3.188T11.5 13"/></svg>
              </span>
              <div class="analytics-deployment__metric-copy"><span>连续推送</span><strong>—</strong><small>暂无推送记录</small></div>
            </li>
          </ul>
          <div class="analytics-deployment__message">
            <span class="analytics-deployment__message-icon">
              <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M12 2l1.6 6.4L20 10l-6.4 1.6L12 18l-1.6-6.4L4 10l6.4-1.6z"/></svg>
            </span>
            <div>
              <strong>暂无推送记录</strong>
              <span>本站未接入 GitHub 推送数据，因此不展示推送活动图。</span>
            </div>
            <svg class="analytics-deployment__message-plane" viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M2.5 19.5q-.425 0-.713-.288T1.5 18.5l.025-2.8L13 12L1.525 8.3L1.5 5.5q0-.425.288-.712T2.5 4.5q.15 0 .3.05L21.5 11.3q.35.15.55.45t.2.65t-.2.65t-.55.45L2.8 19.45q-.15.05-.3.05"/></svg>
          </div>
        </section>

        <!-- 访客活跃时间 -->
        <section class="analytics-panel analytics-activity card-base" aria-labelledby="analytics-activity-title">
          <header class="analytics-panel__header analytics-activity__header">
            <div class="analytics-activity__heading">
              <p class="analytics-section-kicker">VISITOR / ACTIVITY</p>
              <div class="analytics-activity__title-row">
                <h2 id="analytics-activity-title">访客活跃时间</h2>
                <span class="analytics-panel__badge analytics-activity__range">近 30 天</span>
              </div>
              <p class="analytics-panel__description">看看大家通常在什么时候来这里</p>
            </div>
          </header>
          <div class="analytics-activity__body">
            <div class="analytics-activity__heatmap-wrap">
              <div class="analytics-activity__heatmap">
                <div class="analytics-activity__hours" aria-hidden="true">
                  <span v-for="hour in hourLabels" :key="'h' + hour">{{ pad(hour) }}时</span>
                </div>
                <div class="analytics-activity__days" aria-hidden="true">
                  <span v-for="day in weekdays" :key="day">{{ day }}</span>
                </div>
                <div class="analytics-activity__grid" role="grid" aria-label="近 30 天访客按星期与小时的活跃分布">
                  <template v-for="(row, dayIndex) in activityGrid">
                    <button v-for="(value, hourIndex) in row" :key="dayIndex + '-' + hourIndex"
                            type="button" class="analytics-activity__cell"
                            :data-level="cellLevel(value)" :aria-label="cellTitle(dayIndex, hourIndex, value)"
                            :title="cellTitle(dayIndex, hourIndex, value)"
                            @mouseenter="activeCell = { day: dayIndex, hour: hourIndex, value: value }"
                            @mouseleave="activeCell = null"
                            @focus="activeCell = { day: dayIndex, hour: hourIndex, value: value }"></button>
                  </template>
                </div>
                <p class="analytics-activity__note">
                  {{ activeCell ? cellTitle(activeCell.day, activeCell.hour, activeCell.value) : '按北京时间聚合近 30 天访客记录 · 鼠标移入查看具体时段' }}
                </p>
              </div>
            </div>
            <div class="analytics-activity__art" aria-hidden="true">
              <img src="/images/analytics/visitor-activity-character.b82e5a.webp" alt="">
              <p>有你来，<br>真好！<span>♥</span></p>
            </div>
          </div>
          <div class="analytics-activity__stats">
            <div class="analytics-activity__stat analytics-activity__stat--blue">
              <span class="analytics-activity__stat-icon">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M12 20q-3.35 0-5.675-2.325T4 12t2.325-5.675T12 4t5.675 2.325T20 12t-2.325 5.675T12 20m0-2q2.5 0 4.25-1.75T18 12t-1.75-4.25T12 6T7.75 7.75T6 12t1.75 4.25T12 18m1-9v3.6l2.5 1.5l.7-1.15l-2.2-1.3V9z"/></svg>
              </span>
              <div><small>最活跃时间</small><strong>{{ activitySafe.topHourText }}</strong></div>
            </div>
            <div class="analytics-activity__stat analytics-activity__stat--pink">
              <span class="analytics-activity__stat-icon">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M5 22q-.825 0-1.412-.587T3 20V6q0-.825.588-1.412T5 4h1V3q0-.425.288-.712T7 2t.713.288T8 3v1h8V3q0-.425.288-.712T17 2t.713.288T18 3v1h1q.825 0 1.413.588T21 6v14q0 .825-.587 1.413T19 22zm0-2h14V10H5z"/></svg>
              </span>
              <div><small>最活跃星期</small><strong>{{ activitySafe.topWeekdayText }}</strong></div>
            </div>
            <div class="analytics-activity__stat analytics-activity__stat--mint">
              <span class="analytics-activity__stat-icon">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M12 22q-3.35 0-5.675-2.325T4 14q0-.425.288-.712T5 13t.713.288T6 14q0 2.5 1.75 4.25T12 20t4.25-1.75T18 14q0-.425.288-.712T19 13t.713.288T20 14q0 3.35-2.325 5.675T12 22m-3.5-9q-.625 0-1.062-.437T7 11.5t.438-1.062T8.5 10t1.063.438T10 11.5t-.437 1.063T8.5 13m7 0q-.625 0-1.062-.437T14 11.5t.438-1.062T15.5 10t1.063.438T17 11.5t-.437 1.063T15.5 13"/></svg>
              </span>
              <div><small>夜间访问占比</small><strong>{{ activitySafe.nightRatioText }}</strong></div>
            </div>
            <div class="analytics-activity__stat analytics-activity__stat--gold">
              <span class="analytics-activity__stat-icon">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M9 12q-1.65 0-2.825-1.175T5 8t1.175-2.825T9 4t2.825 1.175T13 8t-1.175 2.825T9 12m-6 9q-1.25 0-2.125-.875T0 18v-.7q0-.85.45-1.55t1.2-1.1q1.5-.8 3.225-1.225T9 13t3.125.425T15.35 14.65q.75.4 1.2 1.1t.45 1.55v.7q0 1.25-.875 2.125T14 21zm12 0q.5-.45.75-1.075T16 18.6v-.7q0-.95-.375-1.825T14.55 14.5q1.15.15 2.2.475t1.9.775q.9.5 1.425 1.175t.525 1.55v.7q0 1.25-.875 2.125T18 21z"/></svg>
              </span>
              <div><small>今日访客</small><strong>{{ formatNumber(activitySafe.todayVisitors) }}</strong></div>
            </div>
          </div>
          <div class="analytics-activity__footer">
            <span>按北京时间聚合 · 数据只保留统计维度</span>
            <div class="analytics-activity__legend" aria-label="活跃度色阶">
              <span>少</span>
              <i v-for="level in [0, 1, 2, 3, 4, 5]" :key="'l' + level" :data-level="level"></i>
              <span>多</span>
            </div>
          </div>
        </section>
      </main>

      <aside class="analytics-sidebar">
        <!-- 设备与浏览器 -->
        <section class="analytics-panel analytics-clients card-base" aria-labelledby="analytics-clients-title">
          <header class="analytics-panel__header">
            <div>
              <p class="analytics-section-kicker">VISITORS / DEVICES</p>
              <h2 id="analytics-clients-title">设备与浏览器</h2>
              <p class="analytics-panel__description">访客从哪里来</p>
            </div>
            <svg class="analytics-clients__icon" viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M4 5h16v10H4zm0 12h6v2H4zm8 0h8v2h-8z"/></svg>
          </header>
          <div class="analytics-clients__snapshot analytics-snapshot">
            <div class="analytics-snapshot__breakdown">
              <div class="analytics-snapshot__breakdown-title"><span>设备分布</span><small></small></div>
              <div class="analytics-snapshot__breakdown-body">
                <div class="analytics-snapshot__donut" :class="{ 'is-empty': !deviceSlices.length }">
                  <svg class="analytics-snapshot__donut-svg" viewBox="0 0 100 100" style="--snapshot-donut-progress:360deg" aria-hidden="true">
                    <path v-for="slice in deviceSlices" :key="'d' + slice.name" class="analytics-snapshot__donut-segment"
                          :d="slice.d" :fill="slice.color"></path>
                  </svg>
                  <span>
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M4 5h16v10H4zm0 12h6v2H4zm8 0h8v2h-8z"/></svg>
                  </span>
                </div>
                <ul class="analytics-snapshot__legend">
                  <li v-for="slice in deviceSlices" :key="'dl' + slice.name" class="analytics-snapshot__legend-item">
                    <i :style="{ background: slice.color }"></i>
                    <span>{{ slice.name }}</span>
                    <strong>{{ slice.percent }}%</strong>
                  </li>
                  <li v-if="!deviceSlices.length" class="analytics-snapshot__message">暂无设备数据</li>
                </ul>
              </div>
            </div>
            <div class="analytics-snapshot__breakdown">
              <div class="analytics-snapshot__breakdown-title"><span>浏览器偏好</span><small></small></div>
              <div class="analytics-snapshot__breakdown-body">
                <div class="analytics-snapshot__donut" :class="{ 'is-empty': !browserSlices.length }">
                  <svg class="analytics-snapshot__donut-svg" viewBox="0 0 100 100" style="--snapshot-donut-progress:360deg" aria-hidden="true">
                    <path v-for="slice in browserSlices" :key="'b' + slice.name" class="analytics-snapshot__donut-segment"
                          :d="slice.d" :fill="slice.color"></path>
                  </svg>
                  <span>
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M12 2a10 10 0 1 0 0 20a10 10 0 0 0 0-20m6.9 9h-3.03a15.7 15.7 0 0 0-1.38-5.33A8.02 8.02 0 0 1 18.9 11M12 4.04c.83 1.2 1.55 2.9 1.87 4.96h-3.74C10.45 6.94 11.17 5.24 12 4.04M9.51 5.67A15.7 15.7 0 0 0 8.13 11H5.1a8.02 8.02 0 0 1 4.41-5.33M5.1 13h3.03c.14 2.07.62 3.83 1.38 5.33A8.02 8.02 0 0 1 5.1 13M12 19.96c-.83-1.2-1.55-2.9-1.87-4.96h3.74c-.32 2.06-1.04 3.76-1.87 4.96m2.49-1.63A15.7 15.7 0 0 0 15.87 13h3.03a8.02 8.02 0 0 1-4.41 5.33"/></svg>
                  </span>
                </div>
                <ul class="analytics-snapshot__legend">
                  <li v-for="slice in browserSlices" :key="'bl' + slice.name" class="analytics-snapshot__legend-item">
                    <i :style="{ background: slice.color }"></i>
                    <span>{{ slice.name }}</span>
                    <strong>{{ slice.percent }}%</strong>
                  </li>
                  <li v-if="!browserSlices.length" class="analytics-snapshot__message">暂无浏览器数据</li>
                </ul>
              </div>
            </div>
          </div>
        </section>

        <!-- 访客地域分布 -->
        <section class="analytics-panel analytics-region-card card-base" aria-labelledby="analytics-region-title">
          <header class="analytics-region-card__header">
            <div>
              <p class="analytics-section-kicker">VISITORS / MAP</p>
              <h2 id="analytics-region-title">访客地域分布</h2>
              <p>国内访客分布 <span>· 国外暂不展示</span></p>
            </div>
            <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M12 2a10 10 0 1 0 0 20a10 10 0 0 0 0-20m6.9 9h-3.03a15.7 15.7 0 0 0-1.38-5.33A8.02 8.02 0 0 1 18.9 11M12 4.04c.83 1.2 1.55 2.9 1.87 4.96h-3.74C10.45 6.94 11.17 5.24 12 4.04M9.51 5.67A15.7 15.7 0 0 0 8.13 11H5.1a8.02 8.02 0 0 1 4.41-5.33M5.1 13h3.03c.14 2.07.62 3.83 1.38 5.33A8.02 8.02 0 0 1 5.1 13M12 19.96c-.83-1.2-1.55-2.9-1.87-4.96h3.74c-.32 2.06-1.04 3.76-1.87 4.96m2.49-1.63A15.7 15.7 0 0 0 15.87 13h3.03a8.02 8.02 0 0 1-4.41 5.33"/></svg>
          </header>
          <div class="analytics-region-card__body">
            <div class="analytics-region-card__map-wrap">
              <div class="analytics-region-card__map" role="img" aria-label="中国省级访客地域分布图" @mouseleave="regionHover = ''">
                <img class="analytics-region-card__map-image" :src="mapSource" alt="中国省级行政区边界图" loading="lazy" decoding="async">
                <svg class="analytics-region-card__map-overlay" viewBox="0 0 1000 850" aria-hidden="true">
                  <g>
                    <use v-for="shape in regionBaseShapes" :key="'base' + shape.code"
                         class="analytics-region-card__map-base-fill" :href="mapSource + '#p' + shape.code"></use>
                  </g>
                  <g>
                    <path v-for="(d, index) in chinaOutlinePaths" :key="'outline' + index"
                          class="analytics-region-card__map-outline" :d="d"></path>
                  </g>
                  <g>
                    <use v-for="shape in regionLineShapes" :key="'line' + shape.code"
                         class="analytics-region-card__map-province-line" :href="mapSource + '#p' + shape.code"></use>
                    <path class="analytics-region-card__map-province-line" :d="taiwanPath"></path>
                  </g>
                  <g>
                    <use v-for="shape in regionFills" :key="'fill' + shape.code"
                         class="analytics-region-card__map-fill" :class="'is-level-' + shape.level"
                         :href="mapSource + '#p' + shape.code"
                         :data-active="regionHover === shape.name"
                         @mouseenter="regionHover = shape.name"></use>
                    <use v-if="regionTaiwan.has" class="analytics-region-card__map-taiwan"
                         :class="'is-level-' + regionTaiwan.level" :href="mapSource + '#pTW'"
                         :data-active="regionHover === '台湾'" @mouseenter="regionHover = '台湾'"></use>
                  </g>
                  <g>
                    <circle v-for="point in regionPoints" :key="'point' + point.rank"
                            class="analytics-region-card__map-point" :class="'is-rank-' + point.rank"
                            :cx="point.x" :cy="point.y" :r="point.radius"
                            :data-active="regionHover === point.name"
                            @mouseenter="regionHover = point.name"></circle>
                  </g>
                </svg>
              </div>
            </div>
            <div class="analytics-region-card__ranking">
              <p>TOP 5 省区</p>
              <ol>
                <li v-for="row in regionTop" :key="row.name" class="analytics-region-card__item"
                    :data-active="regionHover === row.name"
                    @mouseenter="regionHover = row.name" @mouseleave="regionHover = ''">
                  <i>{{ pad(row.rank) }}</i>
                  <span>{{ row.name }}</span>
                  <strong>{{ row.percent }}</strong>
                </li>
                <li v-if="!regionTop.length" class="analytics-region-card__empty">暂无地域数据</li>
              </ol>
            </div>
          </div>
          <div class="analytics-region-card__footer">
            <span>按近 30 天国内访客统计 · 国外暂不展示</span>
            <a href="/analytics" @click.prevent>查看数据
              <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M9 5v2h6.6L5 17.6L6.4 19L17 8.4V15h2V5z"/></svg>
            </a>
          </div>
        </section>

        <!-- 流量来源 -->
        <section class="analytics-panel analytics-sources card-base" aria-labelledby="analytics-sources-title">
          <div class="analytics-sources__header">
            <h2 id="analytics-sources-title">流量来源 <span>/ TRAFFIC / SOURCES</span></h2>
            <span class="analytics-sources__range">近 30 天</span>
          </div>
          <div class="analytics-sources__body">
            <div class="analytics-sources__donut" :class="{ 'is-empty': !sourceSlices.length }">
              <svg viewBox="0 0 100 100" style="--source-donut-progress:360deg" aria-hidden="true">
                <path v-for="slice in sourceSlices" :key="'s' + slice.name"
                      class="analytics-sources__donut-segment" :d="slice.d" :fill="slice.color"></path>
              </svg>
              <div class="analytics-sources__donut-label">
                <strong>{{ formatNumber(sources.total || 0) }}</strong>
                <span>本周期访客</span>
              </div>
            </div>
            <ul class="analytics-sources__list">
              <li v-for="item in sources.top" :key="item.name" class="analytics-sources__item">
                <i class="analytics-sources__swatch" :style="{ background: item.color }"></i>
                <span>{{ item.name }}</span>
                <b>{{ item.percent }}%</b>
              </li>
              <li v-if="!sources.top.length" class="analytics-sources__empty">暂无来源数据</li>
            </ul>
          </div>
          <a class="analytics-sources__link" href="/analytics" @click.prevent>查看详细来源
            <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M9 5v2h6.6L5 17.6L6.4 19L17 8.4V15h2V5z"/></svg>
          </a>
        </section>

        <!-- 最近评论 -->
        <section class="analytics-panel analytics-comments analytics-comments--sidebar card-base" aria-labelledby="analytics-comments-title">
          <header class="analytics-panel__header analytics-comments__header">
            <div>
              <p class="analytics-section-kicker">COMMUNITY / RECENT NOTES</p>
              <h2 id="analytics-comments-title">最近评论</h2>
              <p class="analytics-panel__description">文章与留言板里最近的几句话</p>
            </div>
            <div class="analytics-comments__header-meta">
              <span class="analytics-panel__badge">本站评论 · 最新 {{ comments.length }} 条</span>
              <a href="#" @click.prevent="go('/message')">去留言板
                <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M9 5v2h6.6L5 17.6L6.4 19L17 8.4V15h2V5z"/></svg>
              </a>
            </div>
          </header>
          <p v-if="loading" class="analytics-comments__status">正在读取评论…</p>
          <ul v-else-if="comments.length" class="analytics-comments__list">
            <li v-for="comment in comments" :key="comment.id" class="analytics-comment">
              <a class="analytics-comment__link" href="#" @click.prevent="go(comment.path)">
                <span class="analytics-comment__avatar-wrap">
                  <span class="analytics-comment__avatar">
                    <img v-if="comment.avatar" :src="comment.avatar" alt="">
                    <template v-else>{{ avatarLetter(comment.name) }}</template>
                  </span>
                  <span class="identity-v-badge" v-if="hasIdentityV(comment)" title="认证身份">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="#ffffff" d="M6.2 4.5h3.3l2.5 9.6 2.5-9.6h3.3L13.9 20h-3.8z"/></svg>
                  </span>
                  <span v-if="comment.isAnonymous !== 1 && comment.userId !== bossId && !hasIdentityV(comment)"
                        class="art-badge" title="实名用户">
                    <svg viewBox="0 0 24 24" aria-hidden="true">
                      <path d="M5.4 12.6l4.3 4.3 8.9-8.9" fill="none" stroke="#ffffff" stroke-width="3.2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </span>
                </span>
                <span class="analytics-comment__body">
                  <span class="analytics-comment__topline">
                    <span class="analytics-comment__identity">
                      <strong class="art-name" :class="nameClass(comment)" :data-name="comment.name">{{ comment.name }}</strong>
                      <span v-if="!comment.isAnonymous && comment.userId === bossId" class="art-boss" title="主理人">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M4 18h16v2H4zm0-5l4-2.5L10 14l2-5 2 5l2-3.5L20 13v3H4z"/></svg>
                        主理人
                      </span>
                      <span v-else-if="!comment.isAnonymous && comment.userType === 1" class="art-author" title="作者">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M12 3l2.4 5.9l6.3.5l-4.8 4.1l1.5 6.1L12 16.3L6.6 19.6l1.5-6.1L3.3 9.4l6.3-.5z"/></svg>
                        作者
                      </span>
                      <span v-else-if="!comment.isAnonymous && comment.userType === 2" class="art-admin" title="管理员">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M12 2 4 5.5v5.7c0 4.6 3.4 8.9 8 10.3 4.6-1.4 8-5.7 8-10.3V5.5L12 2zm0 4.2 2.1 4.3 4.7.7-3.4 3.3.8 4.7L12 17l-4.2 2.2.8-4.7L11.5 13l-3.4-3.3 4.7-.7z"/></svg>
                        管理员
                      </span>
                      <span v-else-if="!comment.isAnonymous && comment.userType === 5" class="art-ai" title="AI 站长">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" d="M12 21v-8"/><path fill="currentColor" d="M12 13C7.6 13 5.2 10.7 5.2 6.6 9.6 6.6 12 8.9 12 13Z"/><path fill="currentColor" d="M12 11.4c3.5 0 6-2.3 6-6.2-3.5 0-6 2.5-6 6.2Z"/></svg>
                        AI站长
                      </span>
                      <span v-else-if="!comment.isAnonymous && comment.userId" class="art-member" title="会员">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M12 3l2.2 6.4l6.8.2l-5.4 4.1l2 6.5L12 16.4l-5.6 3.8l2-6.5L3 9.6l6.8-.2z"/></svg>
                        会员
                      </span>
                      <time>{{ relativeTime(comment.createTime) }}</time>
                    </span>
                    <span class="analytics-comment__page">评论于 {{ comment.path }}</span>
                  </span>
                  <span class="analytics-comment__text">{{ comment.content || '留下了一条评论' }}</span>
                </span>
              </a>
            </li>
          </ul>
          <div v-else class="analytics-comments__empty">
            <strong>还没有公开评论</strong>
            <small>等第一位路过的朋友留下脚印。</small>
          </div>
          <p class="analytics-comments__status">已展示 {{ comments.length }} 条公开评论 · 内容来自本站评论</p>
        </section>
      </aside>
    </div>

    <!-- 页脚 -->
    <footer class="analytics-footer">
      <div>
        <span class="analytics-footer__eyebrow">A SMALL PUBLIC RECORD</span>
        <strong>谢谢你来过，也谢谢你愿意留下几秒钟。</strong>
      </div>
      <a href="#" @click.prevent="backToAbout">回到关于我
        <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M9 5v2h6.6L5 17.6L6.4 19L17 8.4V15h2V5z"/></svg>
      </a>
    </footer>

    <p v-if="failed" class="analytics-error">数据加载失败，请稍后刷新重试。</p>
  </section>
</template>

<script>
import { mapSource, regionShapeCodes, regionCoordinates, chinaOutlinePaths, taiwanPath } from './chinaMapData'

export default {
  name: 'AnalyticsPanel',
  data() {
    return {
      loading: true,
      failed: false,
      range: 30,
      ranges: [7, 30, 90],
      data: null,
      hoverIndex: -1,
      regionHover: '',
      mapSource: mapSource,
      chinaOutlinePaths: chinaOutlinePaths,
      taiwanPath: taiwanPath,
      activeCell: null,
      weekdays: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      hourLabels: [0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22],
      palette: ['#4d86e8', '#d777c9', '#39b99a', '#df9e45', '#9aa3b2']
    }
  },
  computed: {
    loadState() {
      if (this.failed) return 'offline'
      return this.loading ? 'loading' : 'online'
    },
    statusText() {
      if (this.failed) return '连接失败'
      return this.loading ? '连接中' : '在线'
    },
    updatedText() {
      if (this.failed) return '稍后重试'
      if (this.loading) return '正在读取本站记录'
      return '刚刚更新 · ' + this.range + ' 天区间'
    },
    summaryCards() {
      const summary = (this.data && this.data.summary) || {}
      return [
        { eyebrow: 'ALL-TIME', label: '累计访客', value: summary.visitors, icon: 'group', variant: 'blue', small: '自本站上线起累计' },
        { eyebrow: 'ALL-TIME', label: '累计浏览', value: summary.pageviews, icon: 'visibility', variant: 'mint', small: '自本站上线起累计' },
        { eyebrow: 'TODAY', label: '今日访客', value: summary.todayVisitors, icon: 'person', variant: 'gold', small: '北京时间 00:00 起' },
        { eyebrow: 'TODAY', label: '今日浏览', value: summary.todayPageviews, icon: 'trending', variant: 'violet', small: '实时公开数据' }
      ]
    },
    traffic() {
      return this.data && Array.isArray(this.data.traffic) ? this.data.traffic : []
    },
    hasTraffic() {
      return this.traffic.some(point => Number(point.pageviews) > 0)
    },
    periodData() {
      const fallback = {
        pageviews: 0, visitors: 0, previousPageviews: 0, previousVisitors: 0,
        activeDays: 0, days: 30, peak: 0
      }
      if (!this.data || !this.data.period) return fallback
      return Object.assign(fallback, this.data.period)
    },
    pages() {
      if (!this.data || !Array.isArray(this.data.pages)) return []
      return this.data.pages.filter(page => page && page.path)
    },
    activity() {
      return this.data && this.data.activity ? this.data.activity : null
    },
    activityGrid() {
      if (this.activity && Array.isArray(this.activity.grid) && this.activity.grid.length) {
        return this.activity.grid
      }
      return this.weekdays.map(() => new Array(24).fill(0))
    },
    activitySafe() {
      const source = this.activity || {}
      const hasData = !!source.available
      return {
        topHourText: hasData ? this.pad(source.topHour) + ':00' : '—',
        topWeekdayText: hasData && this.weekdays[source.topWeekday] ? this.weekdays[source.topWeekday] : '—',
        nightRatioText: hasData ? Number(source.nightRatio || 0) + '%' : '—',
        todayVisitors: Number(source.todayVisitors || 0)
      }
    },
    devices() {
      return this.data && this.data.clients && Array.isArray(this.data.clients.devices) ? this.data.clients.devices : []
    },
    browsers() {
      return this.data && this.data.clients && Array.isArray(this.data.clients.browsers) ? this.data.clients.browsers : []
    },
    deviceSlices() {
      return this.pieSlices(this.devices.slice(0, 4), name => this.deviceColor(name))
    },
    browserSlices() {
      return this.pieSlices(this.browsers.slice(0, 5), (name, index) => this.palette[index % this.palette.length])
    },
    sources() {
      const empty = { available: false, total: 0, top: [], slices: [] }
      if (!this.data || !this.data.sources || !Array.isArray(this.data.sources.items)) return empty
      const source = this.data.sources
      if (!source.available || !source.total) return empty
      const colors = ['#4d86e8', '#d777c9', '#39b99a', '#df9e45', '#9aa3b2']
      const top = source.items.slice(0, 5).map((item, index) => ({
        name: item.name,
        value: Number(item.value) || 0,
        percent: Math.round((Number(item.value) || 0) * 100 / source.total),
        color: colors[index % colors.length]
      }))
      return { available: true, total: source.total, top: top }
    },
    sourceSlices() {
      return this.pieSlices(this.sources.top, (name, index) => {
        const item = this.sources.top[index]
        return item ? item.color : this.palette[index % this.palette.length]
      })
    },
    region() {
      const empty = { available: false, total: 0, items: [] }
      const source = this.data && this.data.region
      if (!source || !source.available || !Array.isArray(source.items) || !source.total) return empty
      const items = source.items
        .filter(item => item && item.name && Number(item.value) > 0)
        .map(item => ({ name: item.name, value: Number(item.value), ratio: Number(item.value) / source.total }))
      if (!items.length) return empty
      return { available: true, total: source.total, items: items }
    },
    regionTop() {
      return this.region.items.slice(0, 5).map((item, index) => ({
        name: item.name,
        value: item.value,
        ratio: item.ratio,
        rank: index + 1,
        percent: (item.ratio * 100).toFixed(1) + '%'
      }))
    },
    regionBaseShapes() {
      return regionShapeCodes.map(pair => ({ name: pair[0], code: pair[1] }))
    },
    regionFills() {
      const max = this.region.items.length ? this.region.items[0].value : 1
      const known = {}
      regionShapeCodes.forEach(pair => { known[pair[0]] = pair[1] })
      return this.region.items
        .filter(item => known[item.name] && item.name !== '台湾')
        .map(item => ({
          name: item.name,
          code: known[item.name],
          level: this.regionFillLevel(item.value, max)
        }))
    },
    regionTaiwan() {
      const row = this.region.items.find(item => item.name === '台湾')
      if (!row) return { has: false, level: 0 }
      const max = this.region.items.length ? this.region.items[0].value : 1
      return { has: true, level: this.regionFillLevel(row.value, max) }
    },
    regionLineShapes() {
      return regionShapeCodes.filter(pair => pair[1] !== 'TW').map(pair => ({ name: pair[0], code: pair[1] }))
    },
    regionPoints() {
      return this.regionTop.map(row => {
        const coordinate = regionCoordinates[row.name]
        if (!coordinate) return null
        return {
          name: row.name,
          rank: row.rank,
          x: coordinate[0],
          y: coordinate[1],
          radius: 2.4 + Math.min(3.8, row.ratio * 12)
        }
      }).filter(point => !!point)
    },
    comments() {
      const list = this.data && Array.isArray(this.data.comments) ? this.data.comments : []
      return list.slice(0, 8)
    },
    bossId() {
      return this.$constant.userId
    },
    chart() {
      const points = this.traffic
      const width = 960
      const height = 300
      const left = 42
      const right = 944
      const top = 38
      const bottom = 230
      const count = points.length
      if (!count) {
        return {
          width, height, left, right, top, bottom, points: [], grid: [],
          xLabels: [], pvPath: '', uvPath: '', areaPath: '',
          lastPoint: { x: 0, pvY: 0, uvY: 0 }
        }
      }
      let rawMax = 1
      points.forEach(point => {
        rawMax = Math.max(rawMax, Number(point.pageviews) || 0, Number(point.visitors) || 0)
      })
      const max = this.niceMax(rawMax)
      const step = count > 1 ? (right - left) / (count - 1) : 0
      const xOf = index => count > 1 ? left + index * step : (left + right) / 2
      const yOf = value => bottom - (Number(value) || 0) / max * (bottom - top)
      const plotted = points.map((point, index) => ({
        date: point.date,
        pageviews: Number(point.pageviews) || 0,
        visitors: Number(point.visitors) || 0,
        x: xOf(index),
        pvY: yOf(point.pageviews),
        uvY: yOf(point.visitors)
      }))
      const line = key => plotted.map((point, index) => (index === 0 ? 'M' : 'L') + point.x.toFixed(2) + ' ' + point[key].toFixed(2)).join(' ')
      const pvPath = line('pvY')
      const uvPath = line('uvY')
      const areaPath = pvPath + ' L' + plotted[plotted.length - 1].x.toFixed(2) + ' ' + bottom + ' L' + plotted[0].x.toFixed(2) + ' ' + bottom + ' Z'
      const mid = Math.floor((count - 1) / 2)
      const xLabels = [0, mid, count - 1]
        .filter((value, index, list) => list.indexOf(value) === index)
        .map(index => ({ index: index, x: xOf(index), text: this.formatMonthDay(points[index].date) }))
      return {
        width, height, left, right, top, bottom,
        points: plotted,
        grid: [
          { y: top, label: this.formatNumber(max) },
          { y: 134, label: this.formatNumber(Math.round(max / 2)) },
          { y: bottom, label: '0' }
        ],
        xLabels: xLabels,
        pvPath: pvPath,
        uvPath: uvPath,
        areaPath: areaPath,
        lastPoint: plotted[plotted.length - 1]
      }
    },
    hoverPoint() {
      if (this.hoverIndex < 0 || !this.chart.points[this.hoverIndex]) return null
      return this.chart.points[this.hoverIndex]
    },
    tipLeft() {
      if (!this.hoverPoint) return 12
      const percent = this.hoverPoint.x / this.chart.width * 100
      return Math.min(88, Math.max(12, percent))
    }
  },
  mounted() {
    this.load(false)
  },
  methods: {
    load(replay) {
      this.loading = true
      this.failed = false
      this.$http.get(this.$constant.baseURL + '/webInfo/getAnalytics', { range: this.range })
        .then(res => {
          this.data = res.data || null
          this.loading = false
          this.hoverIndex = -1
          this.activeCell = null
          if (replay) {
            this.range = this.range
          }
        })
        .catch(() => {
          this.loading = false
          this.failed = true
          this.data = null
        })
    },
    changeRange(day) {
      if (this.range === day) return
      this.range = day
      this.load(true)
    },
    onChartMove(event) {
      if (!this.chart.points.length) return
      const rect = event.currentTarget.getBoundingClientRect()
      if (!rect.width) return
      const x = (event.clientX - rect.left) / rect.width * this.chart.width
      const inner = this.chart.right - this.chart.left
      const step = this.chart.points.length > 1 ? inner / (this.chart.points.length - 1) : inner
      let index = step > 0 ? Math.round((x - this.chart.left) / step) : 0
      index = Math.min(this.chart.points.length - 1, Math.max(0, index))
      this.hoverIndex = index
    },
    niceMax(value) {
      if (!value || value <= 0) return 1
      const exponent = Math.pow(10, Math.floor(Math.log10(value)))
      const fraction = value / exponent
      let nice = 10
      if (fraction <= 1) nice = 1
      else if (fraction <= 2) nice = 2
      else if (fraction <= 5) nice = 5
      return nice * exponent
    },
    formatNumber(value) {
      const number = Number(value) || 0
      return number.toLocaleString('en-US')
    },
    pad(value) {
      const number = Number(value) || 0
      return number < 10 ? '0' + number : String(number)
    },
    formatMonthDay(value) {
      if (!value) return ''
      const parts = String(value).split('-')
      if (parts.length < 3) return String(value)
      return Number(parts[1]) + '.' + parts[2]
    },
    changeOf(current, previous) {
      if (!previous) return { up: false, text: '↓ 0%' }
      const diff = current - previous
      const percent = Math.round(Math.abs(diff) * 100 / previous)
      return { up: diff >= 0, text: (diff >= 0 ? '↑ ' : '↓ ') + percent + '%' }
    },
    pageTitle(path) {
      if (path === '/') return '首页'
      return path
    },
    pageSub(path) {
      return path === '/' ? '站点入口' : '页面路径'
    },
    pageBarWidth(value) {
      if (!this.pages.length) return 0
      const max = Math.max.apply(null, this.pages.map(page => Number(page.num) || 0))
      return max ? Math.max(6, Math.round((Number(value) || 0) * 100 / max)) : 0
    },
    cellLevel(value) {
      const max = Number(this.activity && this.activity.max) || 0
      if (!value || !max) return 0
      const ratio = value / max
      if (ratio <= 0.2) return 1
      if (ratio <= 0.4) return 2
      if (ratio <= 0.6) return 3
      if (ratio <= 0.8) return 4
      return 5
    },
    cellTitle(weekday, hour, value) {
      return this.weekdays[weekday] + ' ' + this.pad(hour) + ':00 · ' + value + ' 位访客'
    },
    pieSlices(items, colorOf) {
      const total = items.reduce((sum, item) => sum + (Number(item.value) || 0), 0)
      if (!total) return []
      let angle = -Math.PI / 2
      return items.map((item, index) => {
        const ratio = (Number(item.value) || 0) / total
        const start = angle
        const end = angle + ratio * Math.PI * 2
        angle = end
        const x1 = 50 + 50 * Math.cos(start)
        const y1 = 50 + 50 * Math.sin(start)
        const x2 = 50 + 50 * Math.cos(end)
        const y2 = 50 + 50 * Math.sin(end)
        const largeArc = ratio > 0.5 ? 1 : 0
        return {
          name: item.name,
          percent: Math.round(ratio * 100),
          color: colorOf(item.name, index),
          d: 'M50 50 L' + x1.toFixed(3) + ' ' + y1.toFixed(3) + ' A50 50 0 ' + largeArc + ' 1 ' + x2.toFixed(3) + ' ' + y2.toFixed(3) + ' Z'
        }
      })
    },
    deviceColor(name) {
      const colors = { Mobile: '#4d86e8', Desktop: '#39b99a', Tablet: '#df9e45', '其他': '#9aa3b2', Laptop: '#d777c9' }
      return colors[name] || this.palette[0]
    },
    nameClass(comment) {
      if (!comment || comment.isAnonymous) return ''
      if (comment.userId === this.bossId) return 'art-name-boss'
      if (comment.userType === 1) return 'art-name-author'
      if (comment.userType === 2) return 'art-name-admin'
      if (comment.userType === 5) return 'art-name-ai'
      if (comment.userId) return 'art-name-member'
      return ''
    },
    // 头像认证角标：仅非匿名的主理人 / AI 站长 / 管理员显示
    hasIdentityV(comment) {
      if (!comment || comment.isAnonymous === 1) return false
      return comment.userId === this.bossId || [1, 2, 5].includes(Number(comment.userType))
    },
    relativeTime(value) {
      if (!value) return ''
      const time = new Date(String(value).replace(/-/g, '/')).getTime()
      if (!time) return ''
      const diff = Date.now() - time
      if (diff < 60 * 1000) return '刚刚'
      if (diff < 60 * 60 * 1000) return Math.floor(diff / 60000) + ' 分钟前'
      if (diff < 24 * 60 * 60 * 1000) return Math.floor(diff / 3600000) + ' 小时前'
      if (diff < 7 * 24 * 60 * 60 * 1000) return Math.floor(diff / 86400000) + ' 天前'
      const date = new Date(time)
      return (date.getMonth() + 1) + '月' + date.getDate() + '日'
    },
    avatarLetter(name) {
      return name ? String(name).slice(0, 1) : '匿'
    },
    go(path) {
      if (!path) return
      if (this.$route.path === path) {
        window.scrollTo({ top: 0, behavior: 'smooth' })
        return
      }
      this.$router.push(path).catch(() => {
      })
    },
    regionFillLevel(value, max) {
      const ratio = max > 0 ? value / max : 0
      return Math.min(4, Math.max(0, Math.round(ratio * 4)))
    },
    backToAbout() {
      const target = document.getElementById('about-me') || document.querySelector('.about-container')
      if (target) {
        target.scrollIntoView({ behavior: 'smooth', block: 'start' })
      } else {
        this.$router.push('/about').catch(() => {
        })
      }
    }
  }
}
</script>

<style scoped>
.analytics-page {
  --analytics-blue: #2d7cff;
  --analytics-mint: #39b99a;
  --analytics-gold: #df9e45;
  --analytics-violet: #866de8;
  --analytics-text: color-mix(in oklab, var(--fontColor, #1f2328) 92%, transparent);
  --analytics-text-muted: color-mix(in oklab, var(--fontColor, #1f2328) 58%, transparent);
  --analytics-text-faint: color-mix(in oklab, var(--fontColor, #1f2328) 38%, transparent);
  --analytics-border: color-mix(in oklab, var(--cardBorder, #dfe3e8) 88%, transparent);
  --card-bg: var(--cardBg, #ffffff);
  --muted: var(--cardBorder, #dfe3e8);
  --text-color: var(--fontColor, #1f2328);
  --line-divider: var(--cardBorder, #dfe3e8);
  --deep-text: var(--fontColor, #1f2328);
  --content-meta: var(--greyFont, #7a828c);
  --primary: var(--analytics-blue);
  --radius-large: 24px;
  --font-active-sans: var(--font-sans), "Inter", "PingFang SC", "Microsoft YaHei", sans-serif;
  --ease-decelerate: cubic-bezier(.22, 1, .36, 1);
  --ease-standard: cubic-bezier(.4, 0, .2, 1);
  --duration-normal: .25s;
  position: relative;
  width: 100%;
  padding-bottom: .5rem;
  color: var(--analytics-text);
}

html.dark .analytics-page {
  --analytics-blue: #6ea7ff;
  --analytics-mint: #4fd0b2;
  --analytics-gold: #e8b168;
  --analytics-violet: #a08cff;
  --analytics-text: color-mix(in oklab, var(--fontColor, #e8ecf1) 92%, transparent);
  --analytics-text-muted: color-mix(in oklab, var(--fontColor, #e8ecf1) 58%, transparent);
  --analytics-text-faint: color-mix(in oklab, var(--fontColor, #e8ecf1) 38%, transparent);
  --analytics-border: color-mix(in oklab, var(--cardBorder, #2a3342) 88%, transparent);
  --card-bg: var(--cardBg, #151d28);
  --muted: var(--cardBorder, #2a3342);
  --text-color: var(--fontColor, #e8ecf1);
  --line-divider: var(--cardBorder, #2a3342);
  --deep-text: var(--fontColor, #e8ecf1);
  --content-meta: var(--greyFont, #94a0ae);
}

.analytics-page :deep(*) {
  box-sizing: border-box;
}

.analytics-kicker,
.analytics-footer__eyebrow,
.analytics-section-kicker,
.analytics-summary-card__label {
  font-family: var(--font-active-sans);
  letter-spacing: .12em;
}

/* 页头 */
.analytics-hero {
  position: relative;
  isolation: isolate;
  overflow: hidden;
  padding: clamp(1rem, 2vw, 1.5rem);
  border: 1px solid var(--analytics-border);
  border-radius: var(--radius-large);
  background: linear-gradient(90deg, #fffffff5, #ffffffe5 29%, #ffffff78 61%, #ffffff1f), url(/images/analytics/analytics-hero.7c3f21.webp) center right / cover no-repeat;
  /* 背景图下载完成前的占位底色，避免整块纯白 */
  background-color: #eef4fb;
  box-shadow: 0 1.3rem 3.5rem #234d8214, inset 0 1px #ffffff94;
}

html.dark .analytics-hero {
  background: linear-gradient(90deg, #11192af0, #11192adb 30%, #11192a75 64%, #11192a29), url(/images/analytics/analytics-hero.7c3f21.webp) center right / cover no-repeat;
  background-color: #0f1724;
}

.analytics-hero::before {
  content: "";
  position: absolute;
  z-index: -1;
  top: -5rem;
  right: -3rem;
  width: 25rem;
  height: 15rem;
  border: 1px solid color-mix(in oklab, var(--analytics-blue) 23%, transparent);
  border-radius: 50%;
  transform: rotate(-14deg);
  box-shadow: 0 0 0 1.6rem color-mix(in oklab, var(--analytics-blue) 4%, transparent), 0 0 0 3.2rem color-mix(in oklab, var(--analytics-blue) 3%, transparent);
  pointer-events: none;
}

.analytics-hero::after {
  content: "";
  position: absolute;
  z-index: -1;
  right: 2.7rem;
  bottom: 2.4rem;
  width: 8rem;
  height: 3.5rem;
  background-image: radial-gradient(circle, color-mix(in oklab, var(--analytics-blue) 30%, transparent) .7px, transparent .9px);
  background-size: .65rem .65rem;
  mask-image: linear-gradient(90deg, transparent, #000);
  opacity: .6;
}

.analytics-hero__grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 1.25rem;
  align-items: end;
}

.analytics-kicker {
  display: inline-flex;
  align-items: center;
  gap: .55rem;
  margin-bottom: .55rem;
  color: var(--analytics-blue);
  font-size: .72rem;
  font-weight: 800;
}

.analytics-kicker__signal,
.analytics-status__dot {
  width: .48rem;
  height: .48rem;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 0 .28rem color-mix(in oklab, currentColor 12%, transparent);
}

.analytics-hero h1 {
  margin: 0;
  color: var(--analytics-text);
  font-size: clamp(1.55rem, 3vw, 2.55rem);
  font-weight: 850;
  letter-spacing: -.035em;
  line-height: 1;
}

.analytics-hero__lead {
  max-width: 42rem;
  margin: .6rem 0 0;
  color: var(--analytics-text-muted);
  font-size: clamp(.78rem, 1.1vw, .92rem);
  line-height: 1.55;
}

.analytics-hero__notes {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem 1rem;
  margin-top: .8rem;
  color: var(--analytics-text-faint);
  font-size: .68rem;
}

.analytics-hero__notes span {
  display: inline-flex;
  align-items: center;
  gap: .36rem;
}

.analytics-hero__notes svg {
  color: var(--analytics-blue);
  font-size: 1rem;
  width: 1em;
  height: 1em;
}

.analytics-hero__meta {
  display: grid;
  justify-items: end;
  gap: .7rem;
  min-width: 9rem;
}

.analytics-hero__stamp {
  text-align: right;
}

.analytics-hero__stamp span,
.analytics-hero__stamp small {
  display: block;
  color: var(--analytics-text-faint);
  font-size: .68rem;
}

.analytics-hero__stamp span {
  font-family: var(--font-active-sans);
  letter-spacing: .12em;
}

.analytics-hero__stamp strong {
  display: block;
  margin: .28rem 0;
  color: var(--analytics-text);
  font-size: 1rem;
}

.analytics-hero__rail {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: .85rem;
  margin-top: 1.2rem;
  color: var(--analytics-text-faint);
  font-family: var(--font-active-sans);
  font-size: .63rem;
  letter-spacing: .1em;
}

.analytics-hero__rail span:nth-child(2) {
  height: 1px;
  background: var(--analytics-border);
}

.analytics-status {
  display: inline-flex;
  align-items: center;
  gap: .55rem;
  padding: .45rem .7rem;
  border: 1px solid color-mix(in oklab, var(--analytics-blue) 22%, transparent);
  border-radius: 999px;
  color: var(--analytics-blue);
  background: color-mix(in oklab, var(--analytics-blue) 9%, transparent);
  font-size: .75rem;
  font-weight: 800;
}

.analytics-status[data-state="online"] {
  color: var(--analytics-mint);
  border-color: color-mix(in oklab, var(--analytics-mint) 28%, transparent);
  background: color-mix(in oklab, var(--analytics-mint) 10%, transparent);
}

.analytics-status[data-state="offline"] {
  color: #d46a55;
  border-color: color-mix(in oklab, #d46a55 28%, transparent);
  background: color-mix(in oklab, #d46a55 9%, transparent);
}

.analytics-status[data-state="loading"] .analytics-status__dot {
  animation: analytics-pulse 1.2s ease-in-out infinite;
}

/* 概览卡 */
.analytics-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: .85rem;
  margin: 1rem 0;
}

.analytics-summary-card {
  position: relative;
  min-height: 7.3rem;
  overflow: hidden;
  padding: .82rem .95rem;
  border: 1px solid color-mix(in oklab, var(--analytics-border) 82%, transparent);
  border-radius: calc(var(--radius-large) - .2rem);
  background: color-mix(in oklab, var(--card-bg) 46%, transparent);
  box-shadow: 0 .6rem 1.45rem color-mix(in oklab, var(--analytics-border) 52%, transparent), inset 0 1px #ffffff9e;
  isolation: isolate;
}

.analytics-summary-card::before {
  content: "";
  position: absolute;
  inset: 0;
  z-index: 1;
  background: linear-gradient(90deg, color-mix(in oklab, var(--card-bg) 58%, transparent) 0%, color-mix(in oklab, var(--card-bg) 28%, transparent) 34%, transparent 78%, transparent 100%);
  pointer-events: none;
}

.analytics-summary-card::after {
  content: "";
  position: absolute;
  inset: 0 0 0 10%;
  z-index: 0;
  background-image: var(--summary-art);
  background-position: 78% center;
  background-size: cover;
  background-repeat: no-repeat;
  mask-image: linear-gradient(90deg, transparent 0%, black 5%, black 100%);
  opacity: .86;
  pointer-events: none;
  mix-blend-mode: multiply;
  filter: saturate(1.08) contrast(1.02);
}

html.dark .analytics-summary-card {
  border-color: color-mix(in srgb, var(--summary-accent) 20%, var(--line-divider));
  background: #111a25;
  box-shadow: 0 .55rem 1.2rem #0003, inset 0 1px #ffffff0f;
}

html.dark .analytics-summary-card::before {
  background: linear-gradient(90deg, #090f18f5, #090f18d6 30%, #090f187a 62%, #090f1833);
}

html.dark .analytics-summary-card::after {
  opacity: .9;
  mix-blend-mode: normal;
  filter: saturate(.82) brightness(.68) contrast(1.04);
}

.analytics-summary-card > * {
  position: relative;
  z-index: 2;
}

.analytics-summary-card--blue {
  --summary-accent: var(--analytics-blue);
  --summary-art: url(/images/analytics/summary/summary-visitors.51ab90.webp);
}

.analytics-summary-card--mint {
  --summary-accent: var(--analytics-mint);
  --summary-art: url(/images/analytics/summary/summary-pageviews.9d24c7.webp);
}

.analytics-summary-card--gold {
  --summary-accent: var(--analytics-gold);
  --summary-art: url(/images/analytics/summary/summary-today-visitors.0e6bb3.webp);
}

.analytics-summary-card--violet {
  --summary-accent: var(--analytics-violet);
  --summary-art: url(/images/analytics/summary/summary-today-pageviews.a4e812.webp);
}

.analytics-summary-card__topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--summary-accent);
}

.analytics-summary-card__topline svg {
  font-size: 1.3rem;
  width: 1.3em;
  height: 1.3em;
  opacity: .8;
}

.analytics-summary-card__label {
  font-size: .62rem;
  font-weight: 800;
}

.analytics-summary-card > strong {
  display: block;
  margin-top: .72rem;
  color: var(--analytics-text);
  font-size: clamp(1.3rem, 2.25vw, 1.85rem);
  line-height: 1;
  letter-spacing: -.035em;
  font-variant-numeric: tabular-nums;
  font-feature-settings: "tnum" 1;
}

.analytics-summary-card > span {
  display: block;
  margin-top: .38rem;
  color: var(--analytics-text-muted);
  font-size: .72rem;
  font-weight: 750;
}

.analytics-summary-card > small {
  display: block;
  margin-top: .22rem;
  color: var(--analytics-text-faint);
  font-size: .62rem;
}

/* 布局与面板 */
.analytics-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.8fr) minmax(16rem, .85fr);
  gap: 1rem;
  align-items: start;
}

.analytics-main,
.analytics-sidebar {
  display: grid;
  gap: 1rem;
  min-width: 0;
}

.analytics-panel {
  position: relative;
  min-width: 0;
  overflow: hidden;
  border: 1px solid var(--analytics-border);
  border-radius: var(--radius-large);
  background: color-mix(in oklab, var(--card-bg) 78%, transparent);
  box-shadow: 0 .7rem 1.6rem color-mix(in oklab, var(--analytics-border) 42%, transparent), inset 0 1px #ffffff70;
}

html.dark .analytics-panel {
  background: color-mix(in oklab, var(--card-bg) 86%, transparent);
  box-shadow: 0 .7rem 1.6rem #0002, inset 0 1px #ffffff0d;
}

.analytics-panel__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  padding: 1rem 1.35rem .2rem;
}

.analytics-section-kicker {
  margin: 0;
  color: var(--analytics-blue);
  font-size: .62rem;
  font-weight: 800;
}

.analytics-panel__header h2 {
  margin: .28rem 0 0;
  color: var(--analytics-text);
  font-size: clamp(1.02rem, 1.8vw, 1.22rem);
  font-weight: 840;
  letter-spacing: -.03em;
  line-height: 1.2;
}

.analytics-panel__description {
  margin: .42rem 0 0;
  color: var(--analytics-text-muted);
  font-size: .74rem;
  line-height: 1.55;
}

.analytics-panel__controls {
  display: flex;
  align-items: center;
  gap: .6rem;
}

.analytics-range {
  display: inline-flex;
  padding: .2rem;
  border: 1px solid var(--analytics-border);
  border-radius: 999px;
  background: color-mix(in oklab, var(--muted) 30%, transparent);
}

.analytics-range button {
  padding: .34rem .62rem;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: var(--analytics-text-muted);
  font-size: .66rem;
  font-weight: 700;
  cursor: pointer;
  transition: background var(--duration-normal) var(--ease-standard), color var(--duration-normal) var(--ease-standard);
}

.analytics-range button[aria-pressed="true"] {
  background: var(--card-bg);
  color: var(--analytics-text);
  box-shadow: 0 .2rem .5rem color-mix(in oklab, var(--analytics-border) 60%, transparent);
}

.analytics-refresh {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  padding: 0;
  border: 1px solid var(--analytics-border);
  border-radius: 50%;
  background: color-mix(in oklab, var(--card-bg) 76%, transparent);
  color: var(--analytics-text-muted);
  cursor: pointer;
  transition: color var(--duration-normal) var(--ease-standard), transform var(--duration-normal) var(--ease-standard);
}

.analytics-refresh:hover {
  color: var(--analytics-blue);
  transform: rotate(24deg);
}

.analytics-refresh svg {
  width: 1rem;
  height: 1rem;
}

.analytics-panel__badge {
  display: inline-flex;
  align-items: center;
  padding: .24rem .55rem;
  border: 1px solid color-mix(in oklab, var(--analytics-blue) 24%, transparent);
  border-radius: 999px;
  color: var(--analytics-blue);
  background: color-mix(in oklab, var(--analytics-blue) 9%, transparent);
  font-size: .62rem;
  font-weight: 800;
  white-space: nowrap;
}

/* 访问脉冲 */
.analytics-chart-wrap {
  position: relative;
  min-height: 12.9rem;
  margin: .42rem 0 0;
  padding: .06rem 1.1rem 0 2.2rem;
}

.analytics-chart {
  display: block;
  width: 100%;
  height: clamp(10.5rem, 17vw, 13rem);
  min-height: 0;
  overflow: visible;
  cursor: crosshair;
  touch-action: none;
}

.analytics-chart__grid line {
  stroke: var(--analytics-border);
  stroke-dasharray: 2 7;
}

.analytics-chart__grid line:last-child {
  stroke: color-mix(in oklab, var(--analytics-border) 82%, var(--analytics-text-faint));
  stroke-dasharray: none;
  opacity: .8;
}

.analytics-chart__area {
  fill: url(#analytics-area-gradient);
}

.analytics-chart__line {
  fill: none;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 3;
}

.analytics-chart__line--views {
  stroke: var(--analytics-blue);
  filter: drop-shadow(0 0 .18rem color-mix(in oklab, var(--analytics-blue) 28%, transparent));
}

.analytics-chart__line--sessions {
  stroke: var(--analytics-gold);
  stroke-width: 2.2;
  stroke-dasharray: 5 6;
  opacity: .86;
}

.analytics-chart__point {
  fill: var(--card-bg);
  stroke: var(--analytics-blue);
  stroke-width: 2;
  opacity: 0;
  transition: opacity .2s ease, r .2s ease;
}

.analytics-chart:hover .analytics-chart__point {
  opacity: .85;
}

.analytics-chart__endpoint {
  fill: var(--card-bg);
  stroke-width: 2;
  opacity: 1;
}

.analytics-chart__endpoint--views {
  stroke: var(--analytics-blue);
  filter: drop-shadow(0 0 .2rem color-mix(in oklab, var(--analytics-blue) 32%, transparent));
}

.analytics-chart__endpoint--sessions {
  stroke: var(--analytics-gold);
  opacity: .9;
}

.analytics-chart__guide {
  stroke: color-mix(in oklab, var(--analytics-blue) 42%, transparent);
  stroke-dasharray: 3 4;
  pointer-events: none;
}

.analytics-chart-labels {
  position: absolute;
  top: 1rem;
  bottom: 2.7rem;
  left: 0;
  display: flex;
  width: calc(2.2rem + 4.375%);
  padding-right: .28rem;
  flex-direction: column;
  justify-content: space-between;
  color: var(--analytics-text-faint);
  font-size: .56rem;
  font-variant-numeric: tabular-nums;
  text-align: right;
}

.analytics-chart__x-axis {
  display: flex;
  justify-content: space-between;
  padding: 0 1.667% 0 4.375%;
  color: var(--analytics-text-faint);
  font-size: .58rem;
  font-variant-numeric: tabular-nums;
}

.analytics-chart__tooltip {
  position: absolute;
  top: .1rem;
  left: 12%;
  display: grid;
  gap: .2rem;
  min-width: 7.5rem;
  padding: .6rem .7rem;
  border: 1px solid var(--analytics-border);
  border-radius: .65rem;
  background: color-mix(in oklab, var(--card-bg) 93%, transparent);
  box-shadow: 0 .8rem 1.6rem #234d821f;
  backdrop-filter: blur(14px);
  pointer-events: none;
  font-size: .66rem;
  transform: translate(-.5rem);
}

.analytics-chart__tooltip strong {
  color: var(--analytics-text);
  font-family: var(--font-active-sans);
  font-size: .68rem;
}

.analytics-chart__tooltip span {
  display: flex;
  align-items: center;
  gap: .35rem;
  color: var(--analytics-text-muted);
}

.analytics-chart__tooltip b {
  color: var(--analytics-text);
}

.analytics-legend-dot {
  display: inline-block;
  width: .45rem;
  height: .45rem;
  border-radius: 50%;
}

.analytics-legend-dot--blue {
  background: var(--analytics-blue);
}

.analytics-legend-dot--gold {
  background: var(--analytics-gold);
}

.analytics-chart-empty {
  position: absolute;
  inset: 3rem 3rem 3.5rem;
  display: grid;
  place-content: center;
  justify-items: center;
  gap: .45rem;
  border: 1px dashed var(--analytics-border);
  border-radius: .8rem;
  color: var(--analytics-text-muted);
  background: color-mix(in oklab, var(--muted) 22%, transparent);
  text-align: center;
}

.analytics-chart-empty svg {
  color: var(--analytics-blue);
  width: 1.75rem;
  height: 1.75rem;
}

.analytics-chart-empty strong {
  color: var(--analytics-text);
  font-size: .82rem;
}

.analytics-chart-empty span {
  font-size: .7rem;
}

.analytics-chart.is-empty {
  opacity: .18;
}

.analytics-chart.is-empty .analytics-chart__line,
.analytics-chart.is-empty .analytics-chart__area {
  opacity: 0;
}

.analytics-chart-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin: 0 1.45rem;
  padding: .45rem 0 .62rem;
}

.analytics-chart-legend {
  display: flex;
  flex-wrap: wrap;
  gap: .85rem;
  color: var(--analytics-text-muted);
  font-size: .68rem;
}

.analytics-chart-legend span {
  display: inline-flex;
  align-items: center;
  gap: .34rem;
}

/* 本周期数据 + 排行 */
.analytics-traffic__details {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(0, 1fr);
  gap: 1rem;
  padding: .4rem 1.35rem 1.2rem;
}

.analytics-snapshot {
  min-width: 0;
}

.analytics-snapshot__intro {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: .6rem;
  padding-bottom: .55rem;
  border-bottom: 1px solid var(--analytics-border);
}

.analytics-snapshot__intro strong {
  color: var(--analytics-text);
  font-size: .8rem;
  font-weight: 800;
}

.analytics-snapshot__intro span {
  color: var(--analytics-text-faint);
  font-size: .64rem;
}

.analytics-snapshot__metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: .55rem .9rem;
  margin-top: .65rem;
}

.analytics-snapshot__metric {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: .5rem;
  padding: .38rem .45rem;
  border-radius: .6rem;
  background: color-mix(in oklab, var(--muted) 18%, transparent);
}

.analytics-snapshot__metric-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.5rem;
  height: 1.5rem;
  border-radius: .45rem;
  background: color-mix(in oklab, var(--analytics-blue) 12%, transparent);
  color: var(--analytics-blue);
}

.analytics-snapshot__metric-icon svg {
  width: .92rem;
  height: .92rem;
}

.analytics-snapshot__metric-icon--pink {
  color: #d777c9;
  background: color-mix(in oklab, #d777c9 12%, transparent);
}

.analytics-snapshot__metric-icon--mint {
  color: var(--analytics-mint);
  background: color-mix(in oklab, var(--analytics-mint) 12%, transparent);
}

.analytics-snapshot__metric-icon--gold {
  color: var(--analytics-gold);
  background: color-mix(in oklab, var(--analytics-gold) 12%, transparent);
}

.analytics-snapshot__metric-copy {
  display: grid;
  gap: .1rem;
  min-width: 0;
}

.analytics-snapshot__metric-copy > span {
  color: var(--analytics-text-muted);
  font-size: .64rem;
  font-weight: 700;
}

.analytics-snapshot__metric-copy small {
  color: var(--analytics-text-faint);
  font-size: .58rem;
  white-space: nowrap;
}

.analytics-snapshot__metric-copy small[data-direction="up"] {
  color: var(--analytics-mint);
}

.analytics-snapshot__metric-copy small[data-direction="down"] {
  color: #d46a55;
}

.analytics-snapshot__metric > strong {
  color: var(--analytics-text);
  font-size: .86rem;
  font-variant-numeric: tabular-nums;
}

.analytics-traffic__section-heading {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: .6rem;
  padding-bottom: .55rem;
  border-bottom: 1px solid var(--analytics-border);
}

.analytics-traffic__section-heading strong {
  color: var(--analytics-text);
  font-size: .8rem;
  font-weight: 800;
}

.analytics-traffic__section-heading span {
  color: var(--analytics-text-faint);
  font-size: .64rem;
}

.analytics-ranking {
  display: grid;
  gap: .15rem;
  margin: .45rem 0 0;
  padding: 0;
  list-style: none;
}

.analytics-ranking__item {
  min-width: 0;
}

.analytics-ranking__link {
  position: relative;
  display: grid;
  grid-template-columns: 1.35rem minmax(0, 1fr) auto;
  align-items: center;
  gap: .55rem;
  padding: .42rem .35rem;
  border-radius: .45rem;
  color: inherit;
  text-decoration: none;
  overflow: hidden;
  transition: background var(--duration-normal) var(--ease-standard);
}

.analytics-ranking__link::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 1px;
  background: color-mix(in oklab, var(--analytics-blue) 22%, transparent);
  transform: scaleX(var(--ranking-scale, 0));
  transform-origin: left;
}

.analytics-ranking__link:hover {
  background: color-mix(in oklab, var(--muted) 26%, transparent);
}

.analytics-ranking__index {
  color: var(--analytics-text-faint);
  font-family: var(--font-active-sans);
  font-size: .62rem;
  font-style: normal;
  font-variant-numeric: tabular-nums;
}

.analytics-ranking__copy {
  display: grid;
  gap: .08rem;
  min-width: 0;
}

.analytics-ranking__copy strong {
  color: var(--analytics-text);
  font-size: .7rem;
  font-weight: 750;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.analytics-ranking__copy small {
  color: var(--analytics-text-faint);
  font-size: .58rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.analytics-ranking__link > strong {
  color: var(--analytics-text);
  font-size: .7rem;
  font-variant-numeric: tabular-nums;
}

/* 推送节奏 */
.analytics-deployment__header {
  position: relative;
  min-height: 6.6rem;
  padding-bottom: .9rem;
}

.analytics-deployment__heading {
  position: relative;
  z-index: 1;
  max-width: 60%;
}

.analytics-deployment__title-row {
  display: flex;
  align-items: center;
  gap: .55rem;
  margin-top: .28rem;
}

.analytics-deployment__title-row h2 {
  margin: 0;
}

.analytics-deployment__status {
  display: inline-flex;
  align-items: center;
  padding: .16rem .45rem;
  border: 1px solid var(--analytics-border);
  border-radius: 999px;
  color: var(--analytics-text-faint);
  background: color-mix(in oklab, var(--muted) 26%, transparent);
  font-size: .56rem;
  font-weight: 800;
  white-space: nowrap;
}

.analytics-deployment__note {
  margin: .42rem 0 0;
  color: var(--analytics-text-muted);
  font-size: .74rem;
  line-height: 1.55;
}

.analytics-deployment__art {
  position: absolute;
  right: 1.2rem;
  bottom: .5rem;
  display: flex;
  align-items: flex-end;
  gap: .3rem;
  pointer-events: none;
}

.analytics-deployment__art img {
  width: 6.4rem;
  height: auto;
  display: block;
}

.analytics-deployment__art p {
  margin: 0 0 .5rem;
  color: var(--analytics-text-muted);
  font-size: .58rem;
  line-height: 1.5;
  text-align: right;
}

.analytics-deployment__art > span {
  position: absolute;
  right: -.15rem;
  top: -.2rem;
  color: #e8849b;
  font-size: .9rem;
}

.analytics-deployment__metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: .6rem;
  margin: 0;
  padding: 0 1.35rem 1rem;
  list-style: none;
}

.analytics-deployment__metric {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: center;
  gap: .5rem;
  padding: .55rem .6rem;
  border: 1px solid var(--analytics-border);
  border-radius: .7rem;
  background: color-mix(in oklab, var(--muted) 16%, transparent);
}

.analytics-deployment__metric-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.6rem;
  height: 1.6rem;
  border-radius: .5rem;
  color: var(--analytics-blue);
  background: color-mix(in oklab, var(--analytics-blue) 11%, transparent);
}

.analytics-deployment__metric--pink .analytics-deployment__metric-icon {
  color: #d777c9;
  background: color-mix(in oklab, #d777c9 11%, transparent);
}

.analytics-deployment__metric--violet .analytics-deployment__metric-icon {
  color: var(--analytics-violet);
  background: color-mix(in oklab, var(--analytics-violet) 11%, transparent);
}

.analytics-deployment__metric-icon svg {
  width: .95rem;
  height: .95rem;
}

.analytics-deployment__metric-copy {
  display: grid;
  gap: .06rem;
  min-width: 0;
}

.analytics-deployment__metric-copy span {
  color: var(--analytics-text-faint);
  font-size: .58rem;
}

.analytics-deployment__metric-copy strong {
  color: var(--analytics-text);
  font-size: .78rem;
  font-variant-numeric: tabular-nums;
}

.analytics-deployment__metric-copy small {
  color: var(--analytics-text-faint);
  font-size: .56rem;
}

.analytics-deployment__metric > em {
  display: none;
}

.analytics-deployment__message {
  display: flex;
  align-items: center;
  gap: .75rem;
  margin: 0 1.35rem 1.2rem;
  padding: .85rem 1rem;
  border: 1px dashed var(--analytics-border);
  border-radius: .8rem;
  background: color-mix(in oklab, var(--muted) 18%, transparent);
}

.analytics-deployment__message-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  color: var(--analytics-blue);
  background: color-mix(in oklab, var(--analytics-blue) 10%, transparent);
  flex: none;
}

.analytics-deployment__message-icon svg {
  width: 1.05rem;
  height: 1.05rem;
}

.analytics-deployment__message > div {
  display: grid;
  gap: .18rem;
  min-width: 0;
}

.analytics-deployment__message strong {
  color: var(--analytics-text);
  font-size: .76rem;
}

.analytics-deployment__message span {
  color: var(--analytics-text-muted);
  font-size: .66rem;
  line-height: 1.5;
}

.analytics-deployment__message-plane {
  width: 2.2rem;
  height: 2.2rem;
  margin-left: auto;
  color: color-mix(in oklab, var(--analytics-blue) 42%, transparent);
  flex: none;
}

/* 访客活跃时间 */
.analytics-activity {
  --heat-0: #eef3fb;
  --heat-1: #dce9fd;
  --heat-2: #bcd4fb;
  --heat-3: #8fb7f6;
  --heat-4: #5e93ee;
  --heat-5: #2d7cff;
}

html.dark .analytics-activity {
  --heat-0: #24354d;
  --heat-1: #304b70;
  --heat-2: #3d6598;
  --heat-3: #4d80bf;
  --heat-4: #649ce5;
  --heat-5: #78b1ff;
}

.analytics-activity__header {
  padding-bottom: .5rem;
}

.analytics-activity__title-row {
  display: flex;
  align-items: center;
  gap: .55rem;
  margin-top: .28rem;
}

.analytics-activity__title-row h2 {
  margin: 0;
}

.analytics-activity__range {
  color: var(--analytics-text-faint);
  border-color: var(--analytics-border);
  background: color-mix(in oklab, var(--muted) 24%, transparent);
}

.analytics-activity__body {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(0, .55fr);
  gap: 1rem;
  align-items: end;
  padding: 0 1.35rem;
}

.analytics-activity__heatmap-wrap {
  min-width: 0;
}

.analytics-activity__heatmap {
  display: grid;
  grid-template-columns: 1.7rem minmax(0, 1fr);
  grid-template-rows: auto auto auto;
  gap: .35rem .45rem;
  min-width: 0;
}

.analytics-activity__hours {
  grid-column: 2;
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: .28rem;
  color: var(--analytics-text-muted);
  font-size: .62rem;
  font-variant-numeric: tabular-nums;
  text-align: center;
}

.analytics-activity__hours span {
  min-width: 0;
}

.analytics-activity__days {
  grid-column: 1;
  grid-row: 2;
  display: grid;
  grid-template-rows: repeat(7, minmax(0, 1fr));
  gap: .24rem;
  color: var(--analytics-text-muted);
  font-size: .62rem;
  align-items: center;
  white-space: nowrap;
}

.analytics-activity__days span {
  line-height: 1;
}

.analytics-activity__grid {
  grid-column: 2;
  grid-row: 2;
  display: grid;
  grid-template-columns: repeat(24, minmax(0, 1fr));
  grid-template-rows: repeat(7, minmax(0, 1fr));
  gap: clamp(.18rem, .45vw, .28rem);
  min-width: 0;
}

.analytics-activity__cell {
  width: 100%;
  aspect-ratio: 1;
  min-width: 0;
  padding: 0;
  border: 0;
  border-radius: .24rem;
  background: var(--heat-0);
  cursor: pointer;
  transition: background-color .16s ease, box-shadow .18s ease, filter .18s ease;
}

.analytics-activity__cell[data-level="1"] { background: var(--heat-1); }
.analytics-activity__cell[data-level="2"] { background: var(--heat-2); }
.analytics-activity__cell[data-level="3"] { background: var(--heat-3); }
.analytics-activity__cell[data-level="4"] { background: var(--heat-4); }
.analytics-activity__cell[data-level="5"] { background: var(--heat-5); }

.analytics-activity__cell:hover,
.analytics-activity__cell:focus-visible {
  z-index: 1;
  box-shadow: 0 0 0 2px var(--card-bg), 0 0 0 3px color-mix(in oklab, var(--analytics-blue) 58%, transparent);
  filter: saturate(1.08);
  outline: 0;
}

.analytics-activity__note {
  grid-column: 2;
  grid-row: 3;
  margin: .1rem 0 0;
  color: var(--analytics-text-faint);
  font-size: .6rem;
  min-height: 1em;
}

.analytics-activity__art {
  display: flex;
  align-items: flex-end;
  justify-content: flex-end;
  gap: .25rem;
  position: relative;
  padding-bottom: .2rem;
}

.analytics-activity__art img {
  width: 5.8rem;
  height: auto;
  display: block;
}

.analytics-activity__art p {
  margin: 0 0 .35rem;
  color: var(--analytics-text-muted);
  font-size: .58rem;
  line-height: 1.5;
  text-align: right;
}

.analytics-activity__art p span {
  color: #e8849b;
}

.analytics-activity__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: .6rem;
  padding: .9rem 1.35rem .2rem;
}

.analytics-activity__stat {
  display: flex;
  align-items: center;
  gap: .5rem;
  padding: .5rem .55rem;
  border: 1px solid var(--analytics-border);
  border-radius: .65rem;
  background: color-mix(in oklab, var(--muted) 14%, transparent);
}

.analytics-activity__stat-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.5rem;
  height: 1.5rem;
  border-radius: .45rem;
  color: var(--analytics-blue);
  background: color-mix(in oklab, var(--analytics-blue) 11%, transparent);
  flex: none;
}

.analytics-activity__stat--pink .analytics-activity__stat-icon {
  color: #d777c9;
  background: color-mix(in oklab, #d777c9 11%, transparent);
}

.analytics-activity__stat--mint .analytics-activity__stat-icon {
  color: var(--analytics-mint);
  background: color-mix(in oklab, var(--analytics-mint) 11%, transparent);
}

.analytics-activity__stat--gold .analytics-activity__stat-icon {
  color: var(--analytics-gold);
  background: color-mix(in oklab, var(--analytics-gold) 11%, transparent);
}

.analytics-activity__stat-icon svg {
  width: .92rem;
  height: .92rem;
}

.analytics-activity__stat div {
  display: grid;
  gap: .06rem;
  min-width: 0;
}

.analytics-activity__stat small {
  color: var(--analytics-text-faint);
  font-size: .56rem;
}

.analytics-activity__stat strong {
  color: var(--analytics-text);
  font-size: .76rem;
  font-variant-numeric: tabular-nums;
}

.analytics-activity__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: .8rem;
  flex-wrap: wrap;
  padding: .8rem 1.35rem 1rem;
  color: var(--analytics-text-faint);
  font-size: .58rem;
}

.analytics-activity__legend {
  display: inline-flex;
  align-items: center;
  gap: .18rem;
}

.analytics-activity__legend i {
  width: .55rem;
  height: .55rem;
  border-radius: .14rem;
  background: var(--heat-0);
}

.analytics-activity__legend i[data-level="1"] { background: var(--heat-1); }
.analytics-activity__legend i[data-level="2"] { background: var(--heat-2); }
.analytics-activity__legend i[data-level="3"] { background: var(--heat-3); }
.analytics-activity__legend i[data-level="4"] { background: var(--heat-4); }
.analytics-activity__legend i[data-level="5"] { background: var(--heat-5); }

.analytics-activity__legend span {
  margin: 0 .15rem;
}

/* 设备与浏览器 */
.analytics-clients__icon {
  width: 1.6rem;
  height: 1.6rem;
  color: color-mix(in oklab, var(--analytics-blue) 60%, transparent);
}

.analytics-clients__snapshot {
  display: grid;
  gap: .9rem;
  padding: .8rem 1.35rem 1.1rem;
}

.analytics-snapshot__breakdown {
  display: grid;
  gap: .5rem;
  padding: .6rem .7rem;
  border: 1px solid var(--analytics-border);
  border-radius: .8rem;
  background: color-mix(in oklab, var(--muted) 12%, transparent);
}

.analytics-snapshot__breakdown-title {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: .5rem;
  color: var(--analytics-text-muted);
  font-size: .62rem;
  font-weight: 750;
}

.analytics-snapshot__breakdown-body {
  display: grid;
  grid-template-columns: 4.2rem minmax(0, 1fr);
  gap: .8rem;
  align-items: center;
}

.analytics-snapshot__donut {
  position: relative;
  width: 4.2rem;
  height: 4.2rem;
}

.analytics-snapshot__donut-svg {
  display: block;
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
  mask-image: conic-gradient(#000 0deg, #000 var(--snapshot-donut-progress, 360deg), transparent var(--snapshot-donut-progress, 360deg));
}

.analytics-snapshot__donut-segment {
  stroke: var(--card-bg);
  stroke-width: 1.5;
  stroke-linejoin: round;
}

.analytics-snapshot__donut > span {
  position: absolute;
  inset: 26%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--card-bg);
  color: var(--analytics-text-muted);
}

.analytics-snapshot__donut > span svg {
  width: 1rem;
  height: 1rem;
}

.analytics-snapshot__legend {
  display: grid;
  gap: .22rem;
  margin: 0;
  padding: 0;
  list-style: none;
}

.analytics-snapshot__legend-item {
  display: grid;
  grid-template-columns: .5rem minmax(0, 1fr) auto;
  align-items: center;
  gap: .4rem;
  color: var(--analytics-text-muted);
  font-size: .6rem;
}

.analytics-snapshot__legend-item i {
  width: .45rem;
  height: .45rem;
  border-radius: 50%;
}

.analytics-snapshot__legend-item strong {
  color: var(--analytics-text);
  font-size: .6rem;
  font-variant-numeric: tabular-nums;
}

.analytics-snapshot__message {
  color: var(--analytics-text-faint);
  font-size: .62rem;
}

/* 访客地域分布 */
.analytics-region-card {
  padding: .95rem 1rem .78rem;
  overflow: hidden;
}

.analytics-region-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: .6rem;
}

.analytics-region-card__header .analytics-section-kicker {
  margin: 0 0 .22rem;
  font-size: .5rem;
}

.analytics-region-card__header h2 {
  margin: 0;
  color: var(--analytics-text);
  font-size: .86rem;
  font-weight: 820;
  letter-spacing: -.02em;
  line-height: 1.2;
}

.analytics-region-card__header p:last-child {
  margin: .22rem 0 0;
  color: var(--analytics-text-muted);
  font-size: .58rem;
  line-height: 1.4;
}

.analytics-region-card__header p:last-child span {
  color: var(--analytics-text-faint);
}

.analytics-region-card__header > svg {
  flex: 0 0 auto;
  width: 1.1rem;
  height: 1.1rem;
  color: var(--analytics-blue);
  opacity: .72;
}

.analytics-region-card__body {
  display: grid;
  grid-template-columns: minmax(0, 1.12fr) minmax(6.7rem, .88fr);
  align-items: center;
  gap: .55rem;
  margin-top: .62rem;
  padding: 0;
}

.analytics-region-card__map-wrap {
  min-width: 0;
}

.analytics-region-card__map {
  position: relative;
  width: 100%;
  aspect-ratio: 1000 / 850;
  border-radius: .55rem;
  background: transparent;
  overflow: hidden;
}

.analytics-region-card__map-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  opacity: 0;
}

.analytics-region-card__map-overlay {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  overflow: visible;
}

.analytics-region-card__map-base-fill {
  fill: #eef5fd !important;
  stroke: none !important;
  opacity: .98;
  pointer-events: none;
}

.analytics-region-card__map-outline {
  fill: none;
  stroke: #8db8ee;
  stroke-width: 2.2;
  vector-effect: non-scaling-stroke;
  stroke-linecap: round;
  stroke-linejoin: round;
  opacity: .96;
  pointer-events: none;
}

.analytics-region-card__map-province-line {
  fill: none !important;
  stroke: #7fa6d4 !important;
  stroke-width: 1.6 !important;
  vector-effect: non-scaling-stroke;
  stroke-linecap: round;
  stroke-linejoin: round;
  opacity: 1;
  pointer-events: none;
}

.analytics-region-card__map-fill {
  fill: var(--analytics-blue);
  stroke: #fff;
  stroke-width: 1.2;
  vector-effect: non-scaling-stroke;
  transform-box: fill-box;
  transform-origin: center;
  opacity: .92;
  pointer-events: auto;
  cursor: pointer;
  transition: opacity .52s cubic-bezier(.42, 0, .58, 1), transform .52s cubic-bezier(.42, 0, .58, 1), filter .52s cubic-bezier(.42, 0, .58, 1);
}

.analytics-region-card__map-fill.is-level-1,
.analytics-region-card__map-taiwan.is-level-1 { opacity: .26; }
.analytics-region-card__map-fill.is-level-2,
.analytics-region-card__map-taiwan.is-level-2 { opacity: .4; }
.analytics-region-card__map-fill.is-level-3,
.analytics-region-card__map-taiwan.is-level-3 { opacity: .56; }
.analytics-region-card__map-fill.is-level-4,
.analytics-region-card__map-taiwan.is-level-4 { opacity: .72; }

.analytics-region-card__map-fill[data-active="true"],
.analytics-region-card__map-taiwan[data-active="true"] {
  opacity: .92;
  filter: drop-shadow(0 .18rem .22rem color-mix(in srgb, var(--analytics-blue) 28%, transparent));
  transform: scale(1.08);
}

.analytics-region-card__map-taiwan {
  fill: var(--analytics-blue);
  stroke: #b9cde6;
  stroke-width: 1.2;
  vector-effect: non-scaling-stroke;
  transform-box: fill-box;
  transform-origin: center;
  opacity: .96;
  pointer-events: auto;
  cursor: pointer;
  transition: opacity .52s cubic-bezier(.42, 0, .58, 1), transform .52s cubic-bezier(.42, 0, .58, 1), filter .52s cubic-bezier(.42, 0, .58, 1);
}

.analytics-region-card__map-point {
  fill: var(--analytics-blue);
  stroke: var(--card-bg);
  stroke-width: 3.2;
  transform-box: fill-box;
  transform-origin: center;
  opacity: .94;
  pointer-events: auto;
  cursor: pointer;
  transition: r .18s ease, opacity .18s ease, transform .52s cubic-bezier(.42, 0, .58, 1), filter .52s cubic-bezier(.42, 0, .58, 1);
}

.analytics-region-card__map-point.is-rank-1 { fill: #2d7cff; }
.analytics-region-card__map-point.is-rank-2 { fill: #5d9aff; }
.analytics-region-card__map-point.is-rank-3 { fill: #83b4ff; }
.analytics-region-card__map-point.is-rank-4 { fill: #a4c9ff; }
.analytics-region-card__map-point.is-rank-5 { fill: #c2dcff; }

.analytics-region-card__map-point[data-active="true"] {
  opacity: 1;
  filter: drop-shadow(0 .16rem .2rem color-mix(in srgb, var(--analytics-blue) 30%, transparent));
  transform: scale(1.3);
}

.analytics-region-card__ranking {
  min-width: 0;
}

.analytics-region-card__ranking > p {
  margin: 0 0 .38rem;
  color: var(--analytics-text-faint);
  font-size: .5rem;
  font-weight: 800;
  letter-spacing: .08em;
}

.analytics-region-card__ranking ol {
  display: grid;
  gap: .38rem;
  margin: 0;
  padding: 0;
  list-style: none;
}

.analytics-region-card__item {
  display: grid;
  grid-template-columns: .9rem minmax(0, 1fr) auto;
  align-items: center;
  gap: .28rem;
  min-width: 0;
  padding: .18rem .24rem;
  border-radius: .46rem;
  color: var(--analytics-text-muted);
  font-size: .6rem;
  line-height: 1.1;
  cursor: pointer;
  transition: transform .52s cubic-bezier(.42, 0, .58, 1), background-color .46s ease-in-out, box-shadow .52s cubic-bezier(.42, 0, .58, 1);
}

.analytics-region-card__item[data-active="true"] {
  background: color-mix(in srgb, var(--analytics-blue) 9%, var(--card-bg));
  box-shadow: 0 .32rem .78rem color-mix(in srgb, var(--analytics-blue) 13%, transparent);
  transform: translateY(-1px) scale(1.025);
}

.analytics-region-card__item i {
  display: grid;
  width: .78rem;
  height: .78rem;
  place-items: center;
  border-radius: .22rem;
  color: var(--analytics-blue);
  background: color-mix(in oklab, var(--analytics-blue) 11%, var(--card-bg));
  font-family: var(--font-active-sans);
  font-size: .45rem;
  font-style: normal;
  font-variant-numeric: tabular-nums;
}

.analytics-region-card__item span {
  overflow: hidden;
  color: var(--analytics-text-muted);
  font-size: .6rem;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.analytics-region-card__item strong {
  color: var(--analytics-text-muted);
  font-size: .56rem;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.analytics-region-card__empty {
  color: var(--analytics-text-faint);
  font-size: .58rem;
  line-height: 1.45;
}

.analytics-region-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: .55rem;
  margin-top: .62rem;
  padding: .52rem 0 0;
  border-top: 1px solid var(--analytics-border);
  color: var(--analytics-text-faint);
  font-size: .52rem;
}

.analytics-region-card__footer a {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  gap: .16rem;
  color: var(--analytics-blue);
  font-size: .55rem;
  font-weight: 800;
  text-decoration: none;
  white-space: nowrap;
}

.analytics-region-card__footer a:hover {
  text-decoration: underline;
  text-underline-offset: .18rem;
}

.analytics-region-card__footer > span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.analytics-region-card__footer svg {
  width: .7rem;
  height: .7rem;
}

/* 流量来源 */
.analytics-sources {
  padding: .84rem 1.1rem .62rem;
}

.analytics-sources__header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: .7rem;
  padding: 0;
}

.analytics-sources__header h2 {
  margin: 0;
  color: var(--analytics-text);
  font-size: .86rem;
  font-weight: 800;
  letter-spacing: -.02em;
  line-height: 1.2;
}

.analytics-sources__header h2 span {
  display: inline;
  margin-left: .32rem;
  color: var(--analytics-blue);
  font-family: var(--font-active-sans);
  font-size: .5rem;
  font-weight: 800;
  letter-spacing: .08em;
  white-space: nowrap;
}

.analytics-sources__range {
  flex: 0 0 auto;
  color: var(--analytics-text-faint);
  font-size: .56rem;
  white-space: nowrap;
}

.analytics-sources__body {
  display: grid;
  grid-template-columns: 5.5rem minmax(0, 1fr);
  align-items: center;
  gap: .7rem;
  margin-top: .58rem;
  padding: 0;
}

.analytics-sources__donut {
  position: relative;
  display: grid;
  width: min(5.5rem, 100%);
  aspect-ratio: 1;
  place-items: center;
  border-radius: 50%;
  background: color-mix(in srgb, var(--muted) 28%, var(--card-bg));
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--card-bg) 65%, transparent);
}

.analytics-sources__donut::after {
  position: absolute;
  width: 53%;
  aspect-ratio: 1;
  border-radius: 50%;
  background: var(--card-bg);
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--line-divider) 60%, transparent);
  content: "";
}

.analytics-sources__donut svg {
  position: relative;
  z-index: 1;
  display: block;
  width: 100%;
  height: 100%;
  overflow: visible;
  transform: rotate(-90deg);
  mask-image: conic-gradient(#000 0deg, #000 var(--source-donut-progress, 0deg), transparent var(--source-donut-progress, 0deg));
}

.analytics-sources__donut-segment {
  stroke: var(--card-bg);
  stroke-width: 1.5;
  stroke-linejoin: round;
  opacity: .96;
  cursor: pointer;
}

.analytics-sources__donut-label {
  position: absolute;
  z-index: 2;
  inset: 0;
  display: grid;
  place-content: center;
  justify-items: center;
  background: transparent;
  text-align: center;
  line-height: 1;
  pointer-events: none;
}

.analytics-sources__donut-label strong {
  color: var(--analytics-text);
  font-size: .9rem;
  font-weight: 850;
  letter-spacing: -.035em;
  font-variant-numeric: tabular-nums;
}

.analytics-sources__donut-label span {
  margin-top: .25rem;
  color: var(--analytics-text-faint);
  font-size: .52rem;
  white-space: nowrap;
}

.analytics-sources__list {
  display: grid;
  gap: .3rem;
  margin: 0;
  padding: 0;
  list-style: none;
  min-width: 0;
}

.analytics-sources__item {
  display: grid;
  grid-template-columns: .48rem minmax(0, 1fr) auto;
  align-items: center;
  gap: .36rem;
  min-width: 0;
  padding: .2rem .28rem;
  border-radius: .48rem;
  color: var(--analytics-text-muted);
  font-size: .61rem;
  line-height: 1.2;
  transition: transform .6s cubic-bezier(.42, 0, .58, 1), background-color .52s ease-in-out, box-shadow .6s cubic-bezier(.42, 0, .58, 1);
}

.analytics-sources__item:hover {
  background: color-mix(in srgb, var(--analytics-blue) 10%, var(--card-bg));
  box-shadow: 0 .3rem .7rem color-mix(in srgb, var(--analytics-blue) 12%, transparent);
  transform: translateY(-1px);
}

.analytics-sources__swatch {
  width: .46rem;
  height: .46rem;
  border-radius: 50%;
}

.analytics-sources__item span {
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.analytics-sources__item b {
  color: var(--analytics-text);
  font-size: .6rem;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}

.analytics-sources__empty {
  color: var(--analytics-text-faint);
  font-size: .62rem;
  padding: .3rem .35rem;
}

.analytics-sources__link {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  gap: .28rem;
  width: 100%;
  margin: .52rem 0 0;
  color: var(--analytics-blue);
  font-size: .59rem;
  font-weight: 800;
  text-decoration: none;
}

.analytics-sources__link:hover {
  text-decoration: underline;
  text-underline-offset: .18rem;
}

.analytics-sources__link svg {
  width: .7rem;
  height: .7rem;
}

/* 最近评论 */
.analytics-comments__header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: start;
  gap: .6rem;
  padding-bottom: .5rem;
}

.analytics-comments__header-meta {
  display: grid;
  justify-items: end;
  gap: .35rem;
}

.analytics-comments__header-meta a {
  display: inline-flex;
  align-items: center;
  gap: .2rem;
  color: var(--analytics-blue);
  font-size: .58rem;
  font-weight: 750;
  text-decoration: none;
  white-space: nowrap;
}

.analytics-comments__header-meta a:hover {
  text-decoration: underline;
  text-underline-offset: .18rem;
}

.analytics-comments__header-meta svg {
  width: .68rem;
  height: .68rem;
}

.analytics-comments__list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: .5rem;
  margin: 0;
  padding: .6rem 1.35rem .2rem;
  list-style: none;
}

.analytics-comments--sidebar .analytics-comments__list {
  grid-template-columns: 1fr;
}

.analytics-comment {
  min-width: 0;
}

.analytics-comment__link {
  display: grid;
  grid-template-columns: 2.3rem minmax(0, 1fr);
  gap: .6rem;
  padding: .5rem;
  border-radius: .6rem;
  color: inherit;
  text-decoration: none;
  transition: background var(--duration-normal) var(--ease-standard);
}

.analytics-comment__link:hover {
  background: color-mix(in oklab, var(--muted) 24%, transparent);
}

.analytics-comment__avatar-wrap {
  position: relative;
  width: 2.3rem;
  height: 2.3rem;
  flex: none;
}

.analytics-comment__avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.3rem;
  height: 2.3rem;
  border-radius: 50%;
  overflow: hidden;
  background: color-mix(in oklab, var(--analytics-blue) 14%, transparent);
  color: var(--analytics-blue);
  font-size: .72rem;
  font-weight: 800;
}

.analytics-comment__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.analytics-comment__body {
  display: grid;
  gap: .16rem;
  min-width: 0;
}

.analytics-comment__topline {
  display: grid;
  gap: .08rem;
  min-width: 0;
}

.analytics-comment__identity {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: .24rem;
  min-width: 0;
  color: var(--analytics-text-faint);
  font-size: .6rem;
}

.analytics-comment__identity strong {
  color: var(--analytics-text);
  font-size: .64rem;
  font-weight: 750;
  max-width: 8rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.analytics-comment__identity time {
  color: var(--analytics-text-faint);
  font-size: .54rem;
  white-space: nowrap;
}

.analytics-comment__identity .art-boss,
.analytics-comment__identity .art-author,
.analytics-comment__identity .art-admin,
.analytics-comment__identity .art-ai,
.analytics-comment__identity .art-member {
  display: inline-flex;
  align-items: center;
  gap: .1rem;
  height: 1rem;
  padding: 0 .24rem;
  border-radius: .5rem;
  font-size: .5rem;
  font-weight: 750;
  line-height: 1;
  white-space: nowrap;
}

.analytics-comment__identity .art-boss svg,
.analytics-comment__identity .art-author svg,
.analytics-comment__identity .art-admin svg,
.analytics-comment__identity .art-ai svg,
.analytics-comment__identity .art-member svg {
  width: .58rem;
  height: .58rem;
}

.analytics-comment__page {
  color: var(--analytics-text-faint);
  font-size: .54rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.analytics-comment__text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: var(--analytics-text-muted);
  font-size: .62rem;
  line-height: 1.5;
  word-break: break-word;
}

.analytics-comments__status {
  margin: 0;
  padding: .55rem 1.35rem .8rem;
  color: var(--analytics-text-faint);
  font-size: .54rem;
}

.analytics-comments__empty {
  display: grid;
  gap: .2rem;
  padding: 1rem 1.35rem;
  color: var(--analytics-text-faint);
  font-size: .62rem;
}

.analytics-comments__empty strong {
  color: var(--analytics-text-muted);
  font-size: .68rem;
}

.analytics-comments__empty small {
  font-size: .58rem;
}

/* 页脚 */
.analytics-footer {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-top: 1.25rem;
  padding: 1.1rem 1.35rem;
  overflow: hidden;
  border: 1px solid var(--analytics-border);
  border-radius: var(--radius-large);
  background: color-mix(in oklab, var(--card-bg) 82%, transparent);
  isolation: isolate;
}

.analytics-footer::before {
  position: absolute;
  z-index: 0;
  top: 0;
  bottom: 0;
  left: 0;
  width: 42%;
  border-radius: inherit;
  background: linear-gradient(105deg, transparent 0%, rgb(255 255 255 / .04) 30%, rgb(255 255 255 / .42) 50%, rgb(255 255 255 / .04) 70%, transparent 100%);
  content: "";
  opacity: 0;
  transform: translate(-145%);
  pointer-events: none;
}

.analytics-footer > * {
  position: relative;
  z-index: 1;
}

.analytics-footer__eyebrow {
  display: block;
  margin-bottom: .32rem;
  color: var(--analytics-blue);
  font-size: .62rem;
  font-weight: 800;
}

.analytics-footer strong {
  color: var(--analytics-text);
  font-size: .86rem;
}

.analytics-footer a {
  display: inline-flex;
  align-items: center;
  gap: .35rem;
  color: var(--analytics-blue);
  font-size: .75rem;
  font-weight: 800;
  text-decoration: none;
}

.analytics-footer a:hover {
  text-decoration: underline;
  text-underline-offset: .2rem;
}

.analytics-footer a svg {
  width: .8rem;
  height: .8rem;
}

.analytics-error {
  margin: .8rem 0 0;
  color: #d46a55;
  font-size: .7rem;
}

/* 响应式 */
@media (max-width: 900px) {
  .analytics-layout {
    grid-template-columns: 1fr;
  }

  .analytics-sidebar {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .analytics-hero__grid {
    grid-template-columns: 1fr;
    gap: 1.35rem;
  }

  .analytics-hero__meta {
    justify-items: start;
    grid-template-columns: auto 1fr;
    align-items: end;
    min-width: 0;
  }

  .analytics-hero__stamp {
    text-align: left;
  }

  .analytics-hero__rail {
    margin-top: 1.7rem;
  }

  .analytics-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .analytics-summary-card {
    min-height: 8.9rem;
  }

  .analytics-footer {
    align-items: flex-start;
    flex-direction: column;
  }

  .analytics-traffic__details {
    grid-template-columns: 1fr;
  }

  .analytics-activity__body {
    grid-template-columns: 1fr;
  }

  .analytics-activity__art {
    display: none;
  }

  .analytics-deployment__metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .analytics-region-card__body {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 680px) {
  .analytics-hero {
    background-position: 72% center;
  }

  html.dark .analytics-hero {
    background-position: 72% center;
  }

  .analytics-activity__hours {
    grid-template-columns: repeat(6, minmax(0, 1fr));
  }

  .analytics-sidebar {
    grid-template-columns: 1fr;
  }

  .analytics-activity__stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .analytics-panel__header {
    padding: 1rem 1rem .2rem;
  }

  .analytics-chart-wrap {
    min-height: 12.9rem;
    padding-right: .8rem;
    padding-left: 2rem;
  }

  .analytics-chart-labels {
    width: calc(2rem + 4.375%);
  }

  .analytics-chart-footer {
    align-items: flex-start;
    flex-direction: column;
  }

  .analytics-deployment__art {
    display: none;
  }

  .analytics-deployment__heading {
    max-width: 100%;
  }

  .analytics-traffic__details,
  .analytics-activity__body,
  .analytics-activity__stats,
  .analytics-activity__footer,
  .analytics-deployment__metrics,
  .analytics-deployment__message,
  .analytics-clients__snapshot,
  .analytics-comments__list,
  .analytics-comments__status {
    padding-left: 1rem;
    padding-right: 1rem;
  }
}

@keyframes analytics-pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: .4;
    transform: scale(.8);
  }
}
</style>
