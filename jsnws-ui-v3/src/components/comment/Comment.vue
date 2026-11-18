<template>
  <div>
    <!-- 文章页：规则卡、输入栏、评论标题与列表合并为一张连续大卡片；
         留言页 hideCommentInput=true 时不加外框，继续沿用页面级 message-panel -->
    <div :class="{ 'art-comment-card': !hideCommentInput }">
    <!-- 评论输入区（留言页用 hideCommentInput 隐藏，改用留言页页面级输入栏）：
         留言规则卡片 + 头像/胶囊浮动标签输入框/表情/字数/发送，视觉与留言页保持一致 -->
    <div v-if="!hideCommentInput" class="art-comment-panel">
      <!-- 评论规则：标题 + 四条规则，规则文案沿用留言页，仅标题改为「评论规则」 -->
      <div class="art-comment-rules">
        <div class="art-comment-rules-icon" aria-hidden="true">
          <!-- 浅蓝圆角方形底板 + 白色对话气泡 + 三条蓝色横条（纯内联 SVG，无外链） -->
          <svg viewBox="0 0 24 24" focusable="false" aria-hidden="true">
            <path fill="var(--rules-icon-bubble)"
                  d="M20.5 15.5a2.5 2.5 0 0 1-2.5 2.5H7.5l-4 4V5.5A2.5 2.5 0 0 1 6 3h12a2.5 2.5 0 0 1 2.5 2.5z"/>
            <rect x="7" y="6.3" width="8.8" height="1.7" rx="0.85" fill="currentColor"/>
            <rect x="7" y="9" width="7" height="1.7" rx="0.85" fill="currentColor"/>
            <rect x="7" y="11.7" width="4.6" height="1.7" rx="0.85" fill="currentColor"/>
          </svg>
        </div>
        <div class="art-comment-rules-body">
          <div class="art-comment-rules-title">评论规则</div>
          <ul class="art-comment-rules-list">
            <li>请文明发言，禁止发布违法、违规、广告类内容</li>
            <li>欢迎分享你的想法，也可以提出对网站的建议</li>
            <li>系统会自动记录留言 IP 及地理位置信息用于安全审核</li>
            <li>遵守社区规范，共同维护友好的交流环境</li>
            <li>你的每一条评论都是对我最大的支持</li>
          </ul>
        </div>
      </div>
      <!-- 留言输入栏：提交仍走 Comment 原有的 submitComment 接口与刷新逻辑 -->
      <div class="art-comment-bar">
        <span class="identity-v-wrap">
          <div class="art-comment-bar-avatar">
            <img v-if="userAvatar" :src="userAvatar" alt=""/>
            <span v-else>匿</span>
          </div>
          <span class="identity-v-badge" v-if="currentUserHasIdentity" title="认证身份">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="#ffffff" d="M6.2 4.5h3.3l2.5 9.6 2.5-9.6h3.3L13.9 20h-3.8z"/></svg>
          </span>
        </span>
        <!-- 输入框：表情按钮绝对定位在胶囊内部右端 -->
        <div class="art-comment-bar-input-wrap">
          <FloatInput input-id="art-comment-input"
                      v-model="commentText"
                      label="说点什么，留下你的足迹...">
            <input id="art-comment-input"
                   ref="artCommentInput"
                   class="art-comment-bar-input"
                   v-model="commentText"
                   maxlength="300"
                   @keyup.enter="publishComment"/>
          </FloatInput>
          <el-popover v-model:visible="commentEmojiVisible"
                      :width="316"
                      placement="bottom-end"
                      trigger="click"
                      :show-arrow="false"
                      :offset="10"
                      popper-class="art-comment-emoji-popper">
            <template #reference>
              <button class="art-comment-bar-emoji" type="button" aria-label="表情"
                      @click="rememberCommentCaret">
                <i class="fa fa-smile-o"></i>
              </button>
            </template>
            <EmojiPanel @select="addCommentEmoji"/>
          </el-popover>
        </div>
        <div class="art-comment-bar-actions">
          <span v-if="!isLogin" class="art-comment-bar-login" @click="$router.push('/user')">登录</span>
          <span class="art-comment-bar-count">{{ commentText.length }}/300</span>
          <button class="art-comment-bar-publish" @click="publishComment">发送</button>
        </div>
      </div>
    </div>

    <!-- 评论内容 -->
    <div v-if="comments.length > 0">
      <!-- 留言页旧卡片模板已废弃，统一使用下方 art-comment 模板 -->
      <div v-if="false">
        <div class="msg-list-title">
          <span class="msg-list-title-main">全部留言</span>
          <span class="msg-list-title-divider"></span>
          <span class="msg-list-title-sub">共 {{ total }} 条留言</span>
        </div>
        <div class="msg-card-list" id="comment-content">
          <div class="msg-card"
               v-for="(item, index) in comments"
               :key="index">
            <!-- 留言头部 -->
            <div class="msg-card-head">
              <div class="msg-card-avatar-wrap">
                <div class="msg-card-avatar">
                  <img v-if="!$common.isEmpty(displayAvatar(item))" :src="displayAvatar(item)" alt=""
                       @error="onAvatarError(item)"/>
                  <span v-else>{{ displayName(item).charAt(0) }}</span>
                </div>
                <span class="msg-card-badge" v-if="item.isAnonymous !== 1" title="实名用户">
                  <svg viewBox="0 0 1024 1024" width="9" height="9" aria-hidden="true">
                    <path fill="#ffffff" d="M406.656 726.72 243.712 563.776a38.4 38.4 0 0 1 54.336-54.336l108.608 108.608 319.552-319.552a38.4 38.4 0 1 1 54.336 54.336L460.992 726.72a38.4 38.4 0 0 1-54.336 0z"/>
                  </svg>
                </span>
              </div>
              <div class="msg-card-meta">
                <span class="msg-card-name">{{ displayName(item) }}</span>
                <span class="msg-card-top-tag" v-if="item.isTop === 1">置顶</span>
                <span class="msg-site-badge" v-if="siteName && !item.isAnonymous && item.userId === userId" :title="siteName">
                  <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="currentColor" d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>
                  {{ siteName }}
                </span>
                <span class="msg-card-boss" v-if="!item.isAnonymous && item.userId === userId" title="本站主理人">
                  <svg viewBox="0 0 24 24" width="11" height="11" aria-hidden="true"><path fill="currentColor" d="M5 16 3 5l5.5 5L12 4l3.5 6L21 5l-2 11H5zm0 2h14v2H5z"/></svg>
                  主理人
                </span>
                <span class="msg-card-member" v-if="!item.isAnonymous && item.userId !== userId && item.userType === 4" title="正式会员">正式会员</span>
                <span class="msg-card-author" v-if="!item.isAnonymous && item.userId !== userId && item.userType === 3" title="作者">
                  <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="currentColor" d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a1 1 0 0 0 0-1.41l-2.34-2.34a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg>
                  作　者
                </span>
                <span class="msg-card-admin" v-if="!item.isAnonymous && item.userId !== userId && item.userType === 2" title="管理员">
                  <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="currentColor" d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58a.49.49 0 0 0 .12-.61l-1.92-3.32a.488.488 0 0 0-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54a.484.484 0 0 0-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58a.49.49 0 0 0-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg>
                  管理员
                </span>
                <!-- 地区胶囊：仅当接口真实返回 location 字段时展示，后端当前无该字段 -->
                <span class="msg-card-loc" v-if="!$common.isEmpty(item.location)">
                  <svg viewBox="0 0 1024 1024" width="11" height="11" aria-hidden="true">
                    <path fill="currentColor" d="M512 64C334 64 190 208 190 386c0 242 322 574 322 574s322-332 322-574C834 208 690 64 512 64z m0 448a106 106 0 1 1 0-212 106 106 0 0 1 0 212z"/>
                  </svg>
                  {{ item.location }}
                </span>
                <span class="msg-card-date">{{ formatDate(item.createTime) }}</span>
              </div>
              <div class="msg-card-actions">
                <span class="msg-card-top-btn"
                      v-if="isBoss"
                      @click="toggleTop(item)">
                  {{ item.isTop === 1 ? '取消置顶' : '置顶' }}
                </span>
                <span class="msg-card-reply-count"
                      v-if="!$common.isEmpty(item.childComments) && item.childComments.total > 0">
                  {{ item.childComments.total }} 回复
                </span>
                <span class="msg-card-reply-btn" @click="toggleInlineReply(item, item)">回复</span>
              </div>
            </div>
            <!-- 留言正文 -->
            <div class="msg-card-body" v-html="item.commentContent"></div>
            <!-- 行内回复输入条（卡片模式；文章页仍走弹窗） -->
            <div class="msg-inline-reply" v-if="inlineReplyKey === inlineKey(item, item)">
              <FloatInput :input-id="'inline-reply-' + inlineKey(item, item)"
                          v-model="inlineReplyText"
                          :label="'回复 ' + displayName(item) + '：'">
                <input :id="'inline-reply-' + inlineKey(item, item)"
                       class="msg-inline-input"
                       v-model="inlineReplyText"
                       maxlength="300"
                       @keyup.enter="submitInlineReply(item, item)"/>
              </FloatInput>
              <button class="msg-inline-send" @click="submitInlineReply(item, item)">发送</button>
              <span class="msg-inline-cancel" @click="cancelInlineReply">取消</span>
            </div>
            <!-- 楼中楼回复 -->
            <div class="msg-child-list"
                 v-if="!$common.isEmpty(item.childComments) && !$common.isEmpty(item.childComments.records)">
              <div class="msg-child" v-for="(childItem, i) in visibleChildren(item)" :key="i">
                <div class="msg-card-head">
                  <div class="msg-card-avatar-wrap">
                    <div class="msg-card-avatar msg-child-avatar">
                      <img v-if="!$common.isEmpty(childItem.avatar)" :src="childItem.avatar" alt=""
                           @error="childItem.avatar = ''"/>
                      <span v-else>{{ displayName(childItem).charAt(0) }}</span>
                    </div>
                    <span class="msg-card-badge" v-if="childItem.isAnonymous !== 1" title="实名用户">
                      <svg viewBox="0 0 1024 1024" width="9" height="9" aria-hidden="true">
                        <path fill="#ffffff" d="M406.656 726.72 243.712 563.776a38.4 38.4 0 0 1 54.336-54.336l108.608 108.608 319.552-319.552a38.4 38.4 0 1 1 54.336 54.336L460.992 726.72a38.4 38.4 0 0 1-54.336 0z"/>
                      </svg>
                    </span>
                  </div>
                  <div class="msg-card-meta">
                    <span class="msg-card-name">{{ displayName(childItem) }}</span>
                    <span class="msg-site-badge" v-if="siteName && !childItem.isAnonymous && childItem.userId === userId" :title="siteName">
                      <svg viewBox="0 0 24 24" width="11" height="11" aria-hidden="true"><path fill="currentColor" d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>
                      {{ siteName }}
                    </span>
                    <span class="msg-card-boss" v-if="childItem.userId === userId" title="本站主理人">
                      <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="currentColor" d="M5 16 3 5l5.5 5L12 4l3.5 6L21 5l-2 11H5zm0 2h14v2H5z"/></svg>
                      主理人
                    </span>
                    <span class="msg-card-member" v-if="!childItem.isAnonymous && childItem.userId !== userId && childItem.userType === 4" title="正式会员">正式会员</span>
                    <span class="msg-card-author" v-if="!childItem.isAnonymous && childItem.userId !== userId && childItem.userType === 3" title="作者">
                      <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="currentColor" d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a1 1 0 0 0 0-1.41l-2.34-2.34a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg>
                      作　者
                    </span>
                    <span class="msg-card-admin" v-if="!childItem.isAnonymous && childItem.userId !== userId && childItem.userType === 2" title="管理员">
                      <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="currentColor" d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58a.49.49 0 0 0 .12-.61l-1.92-3.32a.488.488 0 0 0-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54a.484.484 0 0 0-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58a.49.49 0 0 0-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg>
                      管理员
                    </span>
                    <span class="msg-card-loc" v-if="!$common.isEmpty(childItem.location)">
                      <svg viewBox="0 0 1024 1024" width="11" height="11" aria-hidden="true">
                        <path fill="currentColor" d="M512 64C334 64 190 208 190 386c0 242 322 574 322 574s322-332 322-574C834 208 690 64 512 64z m0 448a106 106 0 1 1 0-212 106 106 0 0 1 0 212z"/>
                      </svg>
                      {{ childItem.location }}
                    </span>
                    <span class="msg-card-date">{{ formatDate(childItem.createTime) }}</span>
                  </div>
                  <div class="msg-card-actions">
                    <span class="msg-card-reply-btn" @click="toggleInlineReply(childItem, item)">回复</span>
                  </div>
                </div>
                <div class="msg-card-body msg-child-body">
                  <template v-if="childItem.parentCommentId !== item.id &&
                                  childItem.parentUserId !== childItem.userId">
                    <span class="msg-child-at">@{{ childItem.parentUsername }}</span>
                  </template>
                  <span v-html="childItem.commentContent"></span>
                </div>
                <!-- 行内回复输入条（针对楼中楼） -->
                <div class="msg-inline-reply" v-if="inlineReplyKey === inlineKey(childItem, item)">
                  <FloatInput :input-id="'inline-reply-' + inlineKey(childItem, item)"
                              v-model="inlineReplyText"
                              :label="'回复 ' + displayName(childItem) + '：'">
                    <input :id="'inline-reply-' + inlineKey(childItem, item)"
                           class="msg-inline-input"
                           v-model="inlineReplyText"
                           maxlength="300"
                           @keyup.enter="submitInlineReply(childItem, item)"/>
                  </FloatInput>
                  <button class="msg-inline-send" @click="submitInlineReply(childItem, item)">发送</button>
                  <span class="msg-inline-cancel" @click="cancelInlineReply">取消</span>
                </div>
              </div>
              <div class="msg-child-toggle"
                   v-if="childTotalOf(item) > childPreviewCount"
                   :id="'child-toggle-' + item.id"
                   @click="toggleChildren(item)">
                {{ item.childExpanded ? '收起' : '展开剩余 ' + (childTotalOf(item) - visibleChildren(item).length) + ' 条回复' }}
              </div>
            </div>
          </div>
        </div>
        <!-- 主留言列表：首屏 20 条，超出部分通过点击继续加载，直至全部展示 -->
        <div class="msg-child-toggle"
             v-if="comments.length < pagination.total"
             @click="loadMoreComments">
          展开剩余 {{ pagination.total - comments.length }} 条留言
        </div>
      </div>

      <!-- 统一评论/留言模板（文章页与留言页共用，标题按 cardList 区分） -->
      <div class="art-comment">
        <div class="art-list-title">
          <span class="art-list-title-main">{{ cardList ? '全部留言' : '评论' }}</span>
          <span class="art-list-title-divider"></span>
          <span class="art-list-title-sub">{{ total }} 条{{ cardList ? '留言' : '评论' }}</span>
          <ul class="art-sort">
            <li :class="{ active: sortType === 'time_asc' }" @click="changeSortType('time_asc')">按正序</li>
            <li :class="{ active: sortType === 'time_desc' }" @click="changeSortType('time_desc')">按倒序</li>
            <li :class="{ active: sortType === 'hot' }" @click="changeSortType('hot')">按热度</li>
          </ul>
        </div>
        <div id="comment-content" class="art-card-list">
          <div class="art-card-item" v-for="(item, index) in comments" :key="index">
            <div class="art-user">
              <span class="identity-v-wrap">
                <div class="art-avatar">
                  <img v-if="!$common.isEmpty(displayAvatar(item))" :src="displayAvatar(item)" alt=""
                       @error="onAvatarError(item)"/>
                  <span v-else>{{ displayName(item).charAt(0) }}</span>
                </div>
                <span class="identity-v-badge" v-if="hasIdentityV(item)" title="认证身份">
                  <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="#ffffff" d="M6.2 4.5h3.3l2.5 9.6 2.5-9.6h3.3L13.9 20h-3.8z"/></svg>
                </span>
              </span>
              <span class="art-badge" v-if="item.isAnonymous !== 1 && item.userId !== userId && !hasIdentityV(item)" title="实名用户">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M5.4 12.6l4.3 4.3 8.9-8.9" fill="none" stroke="#ffffff" stroke-width="3.2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
            </div>
            <div class="art-card">
              <div class="art-card-head">
                <span class="art-name"
                      :class="{ 'art-name-boss': !item.isAnonymous && item.userId === userId, 'art-name-admin': !item.isAnonymous && item.userId !== userId && item.userType === 2, 'art-name-author': !item.isAnonymous && item.userId !== userId && item.userType === 3, 'art-name-member': !item.isAnonymous && item.userId !== userId && item.userType === 4, 'art-name-ai': !item.isAnonymous && item.userId !== userId && item.userType === 5 }"
                      :data-name="displayName(item)">{{ displayName(item) }}</span>
                <span class="art-top-tag" v-if="item.isTop === 1">置顶</span>
                <span class="art-site-badge" v-if="siteName && !item.isAnonymous && item.userId === userId" :title="siteName">
                  <svg viewBox="0 0 24 24" width="11" height="11" aria-hidden="true"><path fill="currentColor" d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>
                  {{ siteName }}
                </span>
                <span class="art-boss" v-if="!item.isAnonymous && item.userId === userId" title="本站主理人">
                  <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="currentColor" d="M5 16 3 5l5.5 5L12 4l3.5 6L21 5l-2 11H5zm0 2h14v2H5z"/></svg>
                  主理人
                </span>
                <span class="art-member" v-if="!item.isAnonymous && item.userId !== userId && item.userType === 4" title="正式会员">正式会员</span>
                <span class="art-author" v-if="!item.isAnonymous && item.userId !== userId && item.userType === 3" title="作者">
                  <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="currentColor" d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a1 1 0 0 0 0-1.41l-2.34-2.34a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg>
                  作　者
                </span>
                <span class="art-admin" v-if="!item.isAnonymous && item.userId !== userId && item.userType === 2" title="管理员">
                  <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="currentColor" d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58a.49.49 0 0 0 .12-.61l-1.92-3.32a.488.488 0 0 0-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54a.484.484 0 0 0-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58a.49.49 0 0 0-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg>
                  管理员
                </span>
                <span class="art-ai" v-if="!item.isAnonymous && item.userId !== userId && item.userType === 5" title="AI 站长">
                  <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" d="M12 21v-8"/><path fill="currentColor" d="M12 13C7.6 13 5.2 10.7 5.2 6.6 9.6 6.6 12 8.9 12 13Z"/><path fill="currentColor" d="M12 11.4c3.5 0 6-2.3 6-6.2-3.5 0-6 2.5-6 6.2Z"/></svg>
                  AI站长
                </span>
                <span class="art-date">{{ formatDate(item.createTime) }}</span>
                <span class="art-reply-count"
                      v-if="!$common.isEmpty(item.childComments) && item.childComments.total > 0">
                  {{ item.childComments.total }} 回复
                </span>
                <div class="art-actions">
                  <span class="art-top-btn"
                        v-if="isBoss"
                        @click="toggleTop(item)">
                    {{ item.isTop === 1 ? '取消置顶' : '置顶' }}
                  </span>
                  <span class="art-btn" @click="artToggleInlineReply(item, item)">回复</span>
                </div>
                <button class="art-like-btn" type="button" @click="artLike(item)" :aria-pressed="item._liked || false">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                    <path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8L12 21l8.8-8.6a5.5 5.5 0 0 0 0-7.8Z"/>
                  </svg>
                  <span>{{ item.likeCount || 0 }}</span>
                </button>
              </div>
              <div class="art-meta"
                   v-if="!$common.isEmpty(item.location) || !$common.isEmpty(item.browser) || !$common.isEmpty(item.os)">
                <span class="art-loc" v-if="!$common.isEmpty(item.location)">
                  <svg viewBox="0 0 1024 1024" width="10" height="10" aria-hidden="true" class="art-loc-icon">
                    <path fill="currentColor" d="M512 64C334 64 190 208 190 386c0 242 322 574 322 574s322-332 322-574C834 208 690 64 512 64z m0 448a106 106 0 1 1 0-212 106 106 0 0 1 0 212z"/>
                  </svg>
                  {{ item.location }}
                </span>
                <span class="art-browser" v-if="!$common.isEmpty(item.browser)">
                  <AgentIcon kind="browser" :name="item.browser"></AgentIcon>{{ item.browser }}
                </span>
                <span class="art-os" v-if="!$common.isEmpty(item.os)">
                  <AgentIcon kind="os" :name="item.os"></AgentIcon>{{ item.os }}
                </span>
              </div>
              <div class="art-card-body" v-html="item.commentContent"></div>
              <!-- 行内回复输入条：点「回复」就地展开，文章评论仍要求登录 -->
              <div class="art-inline-reply" v-if="artInlineReplyKey === artInlineKey(item, item)">
                <FloatInput :input-id="'art-inline-reply-' + artInlineKey(item, item)"
                            v-model="artInlineReplyText"
                            :label="'回复 ' + displayName(item) + '：'">
                  <input :id="'art-inline-reply-' + artInlineKey(item, item)"
                         class="art-inline-input"
                         v-model="artInlineReplyText"
                         maxlength="300"
                         @keyup.enter="artSubmitInlineReply(item, item)"/>
                </FloatInput>
                <button class="art-inline-send" @click="artSubmitInlineReply(item, item)">发送</button>
                <span class="art-inline-cancel" @click="artCancelInlineReply">取消</span>
              </div>
              <!-- 楼中楼：参考站同款左侧虚线引用块 -->
              <div class="art-child-list"
                   v-if="!$common.isEmpty(item.childComments) && !$common.isEmpty(item.childComments.records)">
                <div class="art-child" v-for="(childItem, i) in artVisibleChildren(item)" :key="i">
                  <div class="art-user">
                    <span class="identity-v-wrap">
                      <div class="art-avatar art-child-avatar">
                        <img v-if="!$common.isEmpty(childItem.avatar)" :src="childItem.avatar" alt=""
                             @error="childItem.avatar = ''"/>
                        <span v-else>{{ displayName(childItem).charAt(0) }}</span>
                      </div>
                      <span class="identity-v-badge" v-if="hasIdentityV(childItem)" title="认证身份">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="#ffffff" d="M6.2 4.5h3.3l2.5 9.6 2.5-9.6h3.3L13.9 20h-3.8z"/></svg>
                      </span>
                    </span>
                    <span class="art-badge" v-if="childItem.isAnonymous !== 1 && childItem.userId !== userId && !hasIdentityV(childItem)" title="实名用户">
                      <svg viewBox="0 0 24 24" aria-hidden="true">
                        <path d="M5.4 12.6l4.3 4.3 8.9-8.9" fill="none" stroke="#ffffff" stroke-width="3.2" stroke-linecap="round" stroke-linejoin="round"/>
                      </svg>
                    </span>
                  </div>
                  <div class="art-card">
                    <div class="art-card-head">
                      <span class="art-name"
                            :class="{ 'art-name-boss': childItem.userId === userId, 'art-name-admin': !childItem.isAnonymous && childItem.userId !== userId && childItem.userType === 2, 'art-name-author': !childItem.isAnonymous && childItem.userId !== userId && childItem.userType === 3, 'art-name-member': !childItem.isAnonymous && childItem.userId !== userId && childItem.userType === 4, 'art-name-ai': !childItem.isAnonymous && childItem.userId !== userId && childItem.userType === 5 }"
                            :data-name="displayName(childItem)">{{ displayName(childItem) }}</span>
                      <span class="art-site-badge" v-if="siteName && !childItem.isAnonymous && childItem.userId === userId" :title="siteName">
                        <svg viewBox="0 0 24 24" width="11" height="11" aria-hidden="true"><path fill="currentColor" d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>
                        {{ siteName }}
                      </span>
                      <span class="art-boss" v-if="childItem.userId === userId" title="本站主理人">
                        <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="currentColor" d="M5 16 3 5l5.5 5L12 4l3.5 6L21 5l-2 11H5zm0 2h14v2H5z"/></svg>
                        主理人
                      </span>
                      <span class="art-member" v-if="!childItem.isAnonymous && childItem.userId !== userId && childItem.userType === 4" title="正式会员">正式会员</span>
                      <span class="art-author" v-if="!childItem.isAnonymous && childItem.userId !== userId && childItem.userType === 3" title="作者">
                        <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="currentColor" d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a1 1 0 0 0 0-1.41l-2.34-2.34a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg>
                        作　者
                      </span>
                      <span class="art-admin" v-if="!childItem.isAnonymous && childItem.userId !== userId && childItem.userType === 2" title="管理员">
                        <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="currentColor" d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58a.49.49 0 0 0 .12-.61l-1.92-3.32a.488.488 0 0 0-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54a.484.484 0 0 0-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58a.49.49 0 0 0-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg>
                        管理员
                      </span>
                      <span class="art-ai" v-if="!childItem.isAnonymous && childItem.userId !== userId && childItem.userType === 5" title="AI 站长">
                        <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" d="M12 21v-8"/><path fill="currentColor" d="M12 13C7.6 13 5.2 10.7 5.2 6.6 9.6 6.6 12 8.9 12 13Z"/><path fill="currentColor" d="M12 11.4c3.5 0 6-2.3 6-6.2-3.5 0-6 2.5-6 6.2Z"/></svg>
                        AI站长
                      </span>
                      <span class="art-date">{{ formatDate(childItem.createTime) }}</span>
                      <div class="art-actions">
                        <span class="art-btn" @click="artToggleInlineReply(childItem, item)">回复</span>
                        <button class="art-like-btn" type="button" @click="artLike(childItem)" :aria-pressed="childItem._liked || false">
                          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                            <path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8L12 21l8.8-8.6a5.5 5.5 0 0 0 0-7.8Z"/>
                          </svg>
                          <span>{{ childItem.likeCount || 0 }}</span>
                        </button>
                      </div>
                    </div>
                    <div class="art-meta"
                         v-if="!$common.isEmpty(childItem.location) || !$common.isEmpty(childItem.browser) || !$common.isEmpty(childItem.os)">
                      <span class="art-loc" v-if="!$common.isEmpty(childItem.location)">
                        <svg viewBox="0 0 1024 1024" width="10" height="10" aria-hidden="true" class="art-loc-icon">
                          <path fill="currentColor" d="M512 64C334 64 190 208 190 386c0 242 322 574 322 574s322-332 322-574C834 208 690 64 512 64z m0 448a106 106 0 1 1 0-212 106 106 0 0 1 0 212z"/>
                        </svg>
                        {{ childItem.location }}
                      </span>
                      <span class="art-browser" v-if="!$common.isEmpty(childItem.browser)">
                        <AgentIcon kind="browser" :name="childItem.browser"></AgentIcon>{{ childItem.browser }}
                      </span>
                      <span class="art-os" v-if="!$common.isEmpty(childItem.os)">
                        <AgentIcon kind="os" :name="childItem.os"></AgentIcon>{{ childItem.os }}
                      </span>
                    </div>
                    <div class="art-card-body art-child-body">
                      <template v-if="!$common.isEmpty(childItem.parentUsername)">
                        <span class="art-child-at">@{{ childItem.parentUsername }}</span>
                      </template>
                      <span v-html="childItem.commentContent"></span>
                    </div>
                    <div class="art-inline-reply" v-if="artInlineReplyKey === artInlineKey(childItem, item)">
                      <FloatInput :input-id="'art-inline-reply-' + artInlineKey(childItem, item)"
                                  v-model="artInlineReplyText"
                                  :label="'回复 ' + displayName(childItem) + '：'">
                        <input :id="'art-inline-reply-' + artInlineKey(childItem, item)"
                               class="art-inline-input"
                               v-model="artInlineReplyText"
                               maxlength="300"
                               @keyup.enter="artSubmitInlineReply(childItem, item)"/>
                      </FloatInput>
                      <button class="art-inline-send" @click="artSubmitInlineReply(childItem, item)">发送</button>
                      <span class="art-inline-cancel" @click="artCancelInlineReply">取消</span>
                    </div>
                  </div>
                </div>
                <!-- 楼中楼折叠：默认 5 条，入口为描边小按钮 -->
                <div class="art-fold-btn"
                     v-if="artChildTotalOf(item) > artChildPreviewCount"
                     :id="'art-child-toggle-' + item.id"
                     @click="artToggleChildren(item)">
                  {{ item.artChildExpanded ? '收起' : '展开剩余 ' + (artChildTotalOf(item) - artVisibleChildren(item).length) + ' 条回复' }}
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- 主评论列表：首屏 20 条，超出部分继续加载，全部展示后入口消失 -->
        <div class="art-fold-btn art-list-fold"
             v-if="comments.length < pagination.total"
             @click="artLoadMoreComments">
          展开剩余 {{ pagination.total - comments.length }} 条留言
        </div>
      </div>
    </div>

    <div v-else-if="!hideCommentInput" class="myCenter" style="color: var(--greyFont)">
      <i>来发第一个留言啦~</i>
    </div>
    </div>

    <el-dialog title="留言"
               v-model="replyDialogVisible"
               width="30%"
               :before-close="handleClose"
               :append-to-body="true"
               :close-on-click-modal="false"
               destroy-on-close
               center>
      <div>
        <CommentBox :disableGraffiti="true"
                    @submitComment="submitReply">
        </CommentBox>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  // const Graffiti = () => import("./Graffiti");
  import CommentBox from "./CommentBox.vue";
  import EmojiPanel from "./EmojiPanel.vue";
  import ProPage from "@/components/common/ProPage.vue";
  import FloatInput from "@/components/common/FloatInput.vue";
  import AgentIcon from "./AgentIcon.vue";
  import { renderEmojiImages } from "../../utils/emoji";

  export default {
    components: {
      // Graffiti,
      AgentIcon,
      CommentBox,
      EmojiPanel,
      FloatInput,
      ProPage
    },
    props: {
      source: {
        type: Number
      },
      type: {
        type: String
      },
      userId: {
        type: Number
      },
      // 留言页只保留留言列表，隐藏顶部输入框与空态提示；文章页默认 false 不受影响
      hideCommentInput: {
        type: Boolean,
        default: false
      },
      // 留言页使用卡片式留言列表；文章页默认 false，保持原有评论区样式
      cardList: {
        type: Boolean,
        default: false
      }
    },
    data() {
      return {
        // 评论区输入栏：内容、表情面板开关与插入位置
        commentText: "",
        commentEmojiVisible: false,
        commentEmojiCaret: null,
        total: 0,
        replyDialogVisible: false,
        floorComment: {},
        replyComment: {},
        // 卡片模式的行内回复：当前展开的回复目标键与输入内容
        inlineReplyKey: "",
        inlineReplyText: "",
        // 楼中楼折叠阈值：默认最多展示 5 条回复
        childPreviewCount: 5,
        // 文章评论区独立的一套折叠/行内回复状态，与留言页互不共享
        artChildPreviewCount: 5,
        artInlineReplyKey: "",
        artInlineReplyText: "",
        artListLoading: false,
        comments: [],
        // 评论排序：time_desc=按倒序(默认) / time_asc=按正序 / hot=按热度
        sortType: "time_desc",
        // 主留言列表：首屏 20 条，展开时按 20 条一页继续追加
        listLoading: false,
        pagination: {
          current: 1,
          size: 10,
          total: 0,
          source: this.source,
          commentType: this.type,
          floorCommentId: null
        }
      };
    },

    computed: {
      // 输入栏头像与登录入口：未登录时展示匿名占位与「登录」
      isLogin() {
        return !this.$common.isEmpty(this.$store.state.currentUser);
      },
      userAvatar() {
        if (!this.isLogin) {
          return "";
        }
        return this.$store.state.currentUser.avatar || "";
      },
      // 站长判定：与后端 jsnws.boss-user-id 保持一致（前端常量 userId 即站长 id）
      isBoss() {
        const currentUser = this.$store.state.currentUser;
        return !this.$common.isEmpty(currentUser) && currentUser.id === this.userId;
      },
      // 当前登录用户是否需要头像认证角标：主理人 / AI 站长 / 管理员
      currentUserHasIdentity() {
        const currentUser = this.$store.state.currentUser;
        if (this.$common.isEmpty(currentUser)) {
          return false;
        }
        const type = Number(currentUser.userType);
        return currentUser.id === this.userId || type === 1 || type === 2 || type === 5;
      },
      // 站点名称：取 webName，缺失退回 webTitle
      siteName() {
        const info = this.$store.state.webInfo || {};
        if (!this.$common.isEmpty(info.webName)) {
          return info.webName;
        }
        const title = Array.isArray(info.webTitle) ? info.webTitle.join('') : info.webTitle;
        return this.$common.isEmpty(title) ? '' : title;
      }
    },

    created() {
      // 留言页与文章评论区统一首屏取 20 条，超出部分由「展开剩余 N 条留言」继续加载
      this.pagination.size = 20;
      this.getComments(this.pagination);
      this.getTotal();
    },
    methods: {
      // 卡片列表只展示日期部分，数据仍来自接口返回的 createTime
      formatDate(time) {
        return this.$common.isEmpty(time) ? "" : String(time).slice(0, 10);
      },
      // 显示名称：匿名显示"匿名网友"；非匿名优先昵称，没有则用用户名
      displayName(comment) {
        if (comment == null || comment.isAnonymous === 1) return "匿名网友";
        return comment.nickname || comment.username || "匿名网友";
      },
      // 头像认证角标：仅非匿名的主理人 / AI 站长 / 管理员显示
      hasIdentityV(comment) {
        if (comment == null || comment.isAnonymous === 1) return false;
        const type = Number(comment.userType);
        return comment.userId === this.userId || type === 1 || type === 2 || type === 5;
      },
      toPage(page) {
        this.pagination.current = page;
        window.scrollTo({
          top: document.getElementById('comment-content').offsetTop
        });
        this.getComments(this.pagination);
      },
      // 切换评论排序方式，重置到第一页重新拉取
      changeSortType(type) {
        if (this.sortType === type) return;
        this.sortType = type;
        this.pagination.current = 1;
        this.comments = [];
        this.getComments(this.pagination);
      },
      // 点赞评论（楼层或楼中楼），未登录也可点赞，前端防连点
      artLike(comment) {
        if (comment._liked) return;
        comment._liked = true;
        const prev = comment.likeCount || 0;
        comment.likeCount = prev + 1;
        this.$http.post(this.$constant.baseURL + "/comment/likeComment", { commentId: comment.id })
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              comment.likeCount = res.data;
            }
          })
          .catch(() => {
            comment.likeCount = prev;
            comment._liked = false;
          });
      },
      getTotal() {
        this.$http.get(this.$constant.baseURL + "/comment/getCommentCount", {source: this.source, type: this.type})
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.total = res.data;
              // 通知外层（留言页）当前留言总数，便于把列表并入卡片时控制显隐
              this.$emit("changeTotal", this.total);
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      toChildPage(floorComment) {
        floorComment.childComments.current += 1;
        let pagination = {
          current: floorComment.childComments.current,
          size: 5,
          total: 0,
          source: this.source,
          commentType: this.type,
          floorCommentId: floorComment.id
        }
        this.getComments(pagination, floorComment, true);
      },
      // 楼层接口的 childComments 是子评论数组（子评论总数在 childTotal、页码在 childCurrent），
      // 与展开子评论接口返回的 {records, total} 形状不同；这里统一成同一种形状，
      // 否则模板读不到 childComments.records 会一直把楼中楼判为空，直到本人回复后本地被覆盖成 {records} 才显示
      normalizeChildComments(comments) {
        comments.forEach(c => {
          if (Array.isArray(c.childComments)) {
            c.childComments = {
              records: c.childComments,
              total: c.childTotal == null ? c.childComments.length : c.childTotal,
              current: c.childCurrent == null ? 1 : c.childCurrent
            };
          }
        });
      },
      // 楼中楼折叠：默认只渲染前 5 条，展开后渲染已加载的全部回复。
      // 每条留言的展开状态记在自己的 childExpanded 上，互不影响（含置顶留言）。
      visibleChildren(item) {
        const records = item.childComments && item.childComments.records;
        if (this.$common.isEmpty(records)) {
          return [];
        }
        return item.childExpanded || records.length <= this.childPreviewCount
          ? records
          : records.slice(0, this.childPreviewCount);
      },
      // 回复数徽标与折叠文案都用真实总数，不受折叠影响
      childTotalOf(item) {
        return item.childComments && item.childComments.total ? item.childComments.total : 0;
      },
      toggleChildren(item) {
        if (item.childExpanded) {
          this.collapseChildren(item);
          return;
        }
        item.childExpanded = true;
        this.loadMoreChildren(item);
      },
      // 收起：记录收起前入口相对视口的位置，收起后按入口当前位置做一次精确补偿。
      // 目标滚动位置夹取在可达范围内，补偿量不足时停在极限位置由页面吸收多余高度，不强行滚出页面
      collapseChildren(item) {
        const el = document.getElementById("child-toggle-" + item.id);
        if (!el) {
          item.childExpanded = false;
          return;
        }
        const beforeTop = el.getBoundingClientRect().top;
        // 列表在收起瞬间不参与浏览器滚动锚定，避免浏览器自动补偿与下面的补偿叠加
        const list = el.closest(".msg-card-list, .commentInfo-list");
        if (list) {
          list.style.overflowAnchor = "none";
        }
        item.childExpanded = false;
        this.$nextTick(() => {
          const current = window.scrollY;
          const afterTop = el.getBoundingClientRect().top;
          const max = Math.max(document.documentElement.scrollHeight - window.innerHeight, 0);
          const target = Math.min(Math.max(current + afterTop - beforeTop, 0), max);
          if (Math.abs(target - current) > 1) {
            window.scrollTo(0, target);
          }
          if (list) {
            list.style.overflowAnchor = "";
          }
        });
      },
      // 剩余回复未加载时按后端分页（floorCommentId + current/size）继续取，直到全部取回
      loadMoreChildren(item) {
        const childComments = item.childComments;
        if (!childComments || item.childLoading) {
          return;
        }
        if (childComments.records.length >= childComments.total) {
          return;
        }
        item.childLoading = true;
        // 首次返回的子回复是后端按 size=10 取的，不能直接按前端 5 条/页推算页码；
        // 用「已加载条数 / 每页 5 条」向上取整作为已加载页数，从下一页继续取，避免重复或漏取
        const loadedPage = Math.ceil(childComments.records.length / this.childPreviewCount);
        const pagination = {
          current: Math.max(childComments.current || 1, loadedPage) + 1,
          size: this.childPreviewCount,
          total: 0,
          source: this.source,
          commentType: this.type,
          floorCommentId: item.id
        };
        this.$http.post(this.$constant.baseURL + "/comment/listComment", pagination)
          .then((res) => {
            item.childLoading = false;
            if (!this.$common.isEmpty(res.data) && !this.$common.isEmpty(res.data.records)) {
              childComments.current = pagination.current;
              childComments.total = res.data.total;
              childComments.records = childComments.records.concat(res.data.records);
              this.emoji(res.data.records, false);
              this.$nextTick(() => {
                this.$common.imgShow("#comment-content .pictureReg");
              });
              this.loadMoreChildren(item);
            }
          })
          .catch((error) => {
            item.childLoading = false;
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      // 主留言展开：按 size=20 继续取下一页并追加，自动补齐直到全部展示（入口随之消失）
      loadMoreComments() {
        if (this.listLoading || this.comments.length >= this.pagination.total) {
          return;
        }
        this.listLoading = true;
        const pagination = {
          current: this.pagination.current + 1,
          size: this.pagination.size,
          total: 0,
          source: this.source,
          commentType: this.type,
          floorCommentId: null
        };
        this.$http.post(this.$constant.baseURL + "/comment/listComment", pagination)
          .then((res) => {
            this.listLoading = false;
            if (!this.$common.isEmpty(res.data) && !this.$common.isEmpty(res.data.records)) {
              this.pagination.current = pagination.current;
              this.pagination.total = res.data.total;
              this.normalizeChildComments(res.data.records);
              this.comments = this.comments.concat(res.data.records);
              this.emoji(res.data.records, true);
              this.$nextTick(() => {
                this.$common.imgShow("#comment-content .pictureReg");
              });
              this.loadMoreComments();
            }
          })
          .catch((error) => {
            this.listLoading = false;
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      emoji(comments, flag) {
        comments.forEach(c => {
          c.commentContent = c.commentContent.replace(/\n/g, '<br/>');
          c.commentContent = this.$common.faceReg(c.commentContent);
          c.commentContent = this.$common.pictureReg(c.commentContent);
          // 表情/图片标记替换完成后统一净化，用户输入的 HTML 与危险链接不会进入 v-html
          c.commentContent = this.$common.sanitizeHtml(renderEmojiImages(c.commentContent));
          if (flag) {
            if (!this.$common.isEmpty(c.childComments) && !this.$common.isEmpty(c.childComments.records)) {
              c.childComments.records.forEach(cc => {
                cc.commentContent = cc.commentContent.replace(/\n/g, '<br/>');
                cc.commentContent = this.$common.faceReg(cc.commentContent);
                cc.commentContent = this.$common.pictureReg(cc.commentContent);
                cc.commentContent = this.$common.sanitizeHtml(renderEmojiImages(cc.commentContent));
              });
            }
          }
        });
      },
      getComments(pagination, floorComment = {}, isToPage = false) {
        // 主评论列表携带排序方式；楼中楼回复始终按时间正序
        if (pagination.floorCommentId == null) {
          pagination.sortType = this.sortType;
        }
        this.$http.post(this.$constant.baseURL + "/comment/listComment", pagination)
          .then((res) => {
            if (!this.$common.isEmpty(res.data) && !this.$common.isEmpty(res.data.records)) {
              if (this.$common.isEmpty(floorComment)) {
                this.comments = res.data.records;
                this.normalizeChildComments(this.comments);
                pagination.total = res.data.total;
                this.emoji(this.comments, true);
              } else {
                if (isToPage === false) {
                  floorComment.childComments = res.data;
                  // 该楼处于展开态时，重新拉取第一页后继续补齐剩余回复（新回复按时间排在后段也不会被藏住）
                  if (floorComment.childExpanded) {
                    this.loadMoreChildren(floorComment);
                  }
                } else {
                  floorComment.childComments.total = res.data.total;
                  floorComment.childComments.records = floorComment.childComments.records.concat(res.data.records);
                }
                this.emoji(floorComment.childComments.records, false);
              }
              this.$nextTick(() => {
                this.$common.imgShow("#comment-content .pictureReg");
              });
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      addGraffitiComment(graffitiComment) {
        this.submitComment(graffitiComment);
      },
      // 打开表情面板前记录光标位置，表情插入到光标处
      rememberCommentCaret() {
        const input = this.$refs.artCommentInput;
        this.commentEmojiCaret = input ? input.selectionStart : this.commentText.length;
      },
      // 插入表情：面板抛出的就是 Waline 语法 token，走 v-model 同步，字数统计与 300 字上限一并生效
      addCommentEmoji(token) {
        const length = this.commentText.length;
        const caret = this.commentEmojiCaret === null || this.commentEmojiCaret > length
          ? length : this.commentEmojiCaret;
        const next = this.commentText.slice(0, caret) + token + this.commentText.slice(caret);
        if (next.length > 300) {
          this.$message({
            message: "留言内容不能超过300字！",
            type: "warning"
          });
          return;
        }
        this.commentText = next;
        this.commentEmojiCaret = caret + token.length;
      },
      // 发送留言：校验后复用原有 submitComment，接口与列表刷新逻辑不变
      publishComment() {
        if (this.commentText.trim() === "") {
          this.$message({
            message: "你还没写呢~",
            type: "warning"
          });
          return;
        }
        this.submitComment(this.commentText.trim());
        this.commentText = "";
      },
      submitComment(commentContent) {
        let comment = {
          source: this.source,
          commentType: this.type,
          commentContent: commentContent
        };

        this.$http.post(this.$constant.baseURL + "/comment/saveComment", comment)
          .then((res) => {
            this.$message({
              type: 'success',
              message: '发送成功'
            });
            this.pagination = {
              current: 1,
              size: this.pagination.size,
              total: 0,
              source: this.source,
              commentType: this.type,
              floorCommentId: null
            }
            this.getComments(this.pagination);
            this.getTotal();
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      submitReply(commentContent) {
        let comment = {
          source: this.source,
          commentType: this.type,
          floorCommentId: this.floorComment.id,
          commentContent: commentContent,
          parentCommentId: this.replyComment.id,
          parentUserId: this.replyComment.userId
        };

        let floorComment = this.floorComment;

        // 发表回复后强制展开该楼并补齐剩余回复，保证自己刚发的回复一定可见
        floorComment.childExpanded = true;

        this.$http.post(this.$constant.baseURL + "/comment/saveComment", comment)
          .then((res) => {
            this.$message({
              type: 'success',
              message: '发送成功'
            });
            let pagination = {
              current: 1,
              size: 5,
              total: 0,
              source: this.source,
              commentType: this.type,
              floorCommentId: floorComment.id
            }
            this.getComments(pagination, floorComment);
            this.getTotal();
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
        this.handleClose();
      },
      replyDialog(comment, floorComment) {
        this.replyComment = comment;
        this.floorComment = floorComment;
        this.replyDialogVisible = true;
      },
      inlineKey(comment, floorComment) {
        return floorComment.id + "-" + comment.id;
      },
      // 匿名留言优先使用匿名随机头像，普通留言沿用账号头像
      displayAvatar(comment) {
        return comment.isAnonymous === 1 && !this.$common.isEmpty(comment.anonymousAvatar)
          ? comment.anonymousAvatar
          : comment.avatar;
      },
      onAvatarError(comment) {
        if (comment.isAnonymous === 1) {
          comment.anonymousAvatar = "";
        } else {
          comment.avatar = "";
        }
      },
      // 置顶 / 取消置顶：仅站长可见入口，成功后回到第一页重新拉取列表
      toggleTop(item) {
        const top = item.isTop !== 1;
        this.$http.post(this.$constant.baseURL + "/comment/topComment", {
          id: item.id,
          top: top
        })
          .then(() => {
            this.$message({
              type: "success",
              message: top ? "已置顶" : "已取消置顶"
            });
            this.pagination.current = 1;
            this.getComments(this.pagination);
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      toggleInlineReply(comment, floorComment) {
        if (this.inlineReplyKey === this.inlineKey(comment, floorComment)) {
          this.cancelInlineReply();
          return;
        }
        this.inlineReplyKey = this.inlineKey(comment, floorComment);
        this.inlineReplyText = "";
      },
      cancelInlineReply() {
        this.inlineReplyKey = "";
        this.inlineReplyText = "";
      },
      submitInlineReply(comment, floorComment) {
        if (this.$common.isEmpty(this.$store.state.currentUser)) {
          this.$message({
            message: "请先登录！",
            type: "error"
          });
          return;
        }
        const content = (this.inlineReplyText || "").trim();
        if (content === "") {
          this.$message({
            message: "你还没写呢~",
            type: "warning"
          });
          return;
        }
        this.replyComment = comment;
        this.floorComment = floorComment;
        this.submitReply(content);
        this.cancelInlineReply();
      },
      // ==================== 文章评论区（独立一套，不复用留言页的折叠/行内回复状态） ====================
      artInlineKey(comment, floorComment) {
        return "art-" + floorComment.id + "-" + comment.id;
      },
      artVisibleChildren(item) {
        const records = item.childComments && item.childComments.records;
        if (this.$common.isEmpty(records)) {
          return [];
        }
        return item.artChildExpanded || records.length <= this.artChildPreviewCount
          ? records
          : records.slice(0, this.artChildPreviewCount);
      },
      artChildTotalOf(item) {
        return item.childComments && item.childComments.total ? item.childComments.total : 0;
      },
      artToggleChildren(item) {
        if (item.artChildExpanded) {
          this.artCollapseChildren(item);
          return;
        }
        item.artChildExpanded = true;
        this.artLoadMoreChildren(item);
      },
      // 收起：记录入口收起前相对视口的位置，收起后精确补偿；补偿目标夹取在可达范围内
      artCollapseChildren(item) {
        const el = document.getElementById("art-child-toggle-" + item.id);
        if (!el) {
          item.artChildExpanded = false;
          return;
        }
        const beforeTop = el.getBoundingClientRect().top;
        const list = el.closest(".art-card-list");
        if (list) {
          list.style.overflowAnchor = "none";
        }
        item.artChildExpanded = false;
        this.$nextTick(() => {
          const current = window.scrollY;
          const afterTop = el.getBoundingClientRect().top;
          const max = Math.max(document.documentElement.scrollHeight - window.innerHeight, 0);
          const target = Math.min(Math.max(current + afterTop - beforeTop, 0), max);
          if (Math.abs(target - current) > 1) {
            window.scrollTo(0, target);
          }
          if (list) {
            list.style.overflowAnchor = "";
          }
        });
      },
      // 剩余回复未加载时按 floorCommentId + current/size 继续取，直到全部取回
      artLoadMoreChildren(item) {
        const childComments = item.childComments;
        if (!childComments || item.artChildLoading) {
          return;
        }
        if (childComments.records.length >= childComments.total) {
          return;
        }
        item.artChildLoading = true;
        const loadedPage = Math.ceil(childComments.records.length / this.artChildPreviewCount);
        const pagination = {
          current: Math.max(childComments.current || 1, loadedPage) + 1,
          size: this.artChildPreviewCount,
          total: 0,
          source: this.source,
          commentType: this.type,
          floorCommentId: item.id
        };
        this.$http.post(this.$constant.baseURL + "/comment/listComment", pagination)
          .then((res) => {
            item.artChildLoading = false;
            if (!this.$common.isEmpty(res.data) && !this.$common.isEmpty(res.data.records)) {
              childComments.current = pagination.current;
              childComments.total = res.data.total;
              childComments.records = childComments.records.concat(res.data.records);
              this.emoji(res.data.records, false);
              this.$nextTick(() => {
                this.$common.imgShow("#comment-content .pictureReg");
              });
              this.artLoadMoreChildren(item);
            }
          })
          .catch((error) => {
            item.artChildLoading = false;
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      // 主评论展开：按 size=20 继续取下一页并追加，补齐后入口消失
      artLoadMoreComments() {
        if (this.artListLoading || this.comments.length >= this.pagination.total) {
          return;
        }
        this.artListLoading = true;
        const pagination = {
          current: this.pagination.current + 1,
          size: this.pagination.size,
          total: 0,
          source: this.source,
          commentType: this.type,
          floorCommentId: null
        };
        this.$http.post(this.$constant.baseURL + "/comment/listComment", pagination)
          .then((res) => {
            this.artListLoading = false;
            if (!this.$common.isEmpty(res.data) && !this.$common.isEmpty(res.data.records)) {
              this.pagination.current = pagination.current;
              this.pagination.total = res.data.total;
              this.normalizeChildComments(res.data.records);
              this.comments = this.comments.concat(res.data.records);
              this.emoji(res.data.records, true);
              this.$nextTick(() => {
                this.$common.imgShow("#comment-content .pictureReg");
              });
              this.artLoadMoreComments();
            }
          })
          .catch((error) => {
            this.artListLoading = false;
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      artToggleInlineReply(comment, floorComment) {
        // 文章评论不支持匿名：未登录不展开输入条，直接提示
        if (this.$common.isEmpty(this.$store.state.currentUser)) {
          this.$message({
            message: "请先登录！",
            type: "error"
          });
          return;
        }
        if (this.artInlineReplyKey === this.artInlineKey(comment, floorComment)) {
          this.artCancelInlineReply();
          return;
        }
        this.artInlineReplyKey = this.artInlineKey(comment, floorComment);
        this.artInlineReplyText = "";
      },
      artCancelInlineReply() {
        this.artInlineReplyKey = "";
        this.artInlineReplyText = "";
      },
      artSubmitInlineReply(comment, floorComment) {
        if (this.$common.isEmpty(this.$store.state.currentUser)) {
          this.$message({
            message: "请先登录！",
            type: "error"
          });
          return;
        }
        const content = (this.artInlineReplyText || "").trim();
        if (content === "") {
          this.$message({
            message: "你还没写呢~",
            type: "warning"
          });
          return;
        }
        this.artSubmitReply(comment, floorComment, content);
        this.artCancelInlineReply();
      },
      artSubmitReply(targetComment, floorComment, commentContent) {
        const comment = {
          source: this.source,
          commentType: this.type,
          floorCommentId: floorComment.id,
          commentContent: commentContent,
          parentCommentId: targetComment.id,
          parentUserId: targetComment.userId
        };
        // 该楼保持展开，拉取第一页后再补齐剩余回复，保证刚发的回复可见
        floorComment.artChildExpanded = true;
        this.$http.post(this.$constant.baseURL + "/comment/saveComment", comment)
          .then((res) => {
            this.$message({
              type: 'success',
              message: '发送成功'
            });
            this.artRefreshChildren(floorComment);
            this.getTotal();
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      artRefreshChildren(item) {
        const pagination = {
          current: 1,
          size: 10,
          total: 0,
          source: this.source,
          commentType: this.type,
          floorCommentId: item.id
        };
        this.$http.post(this.$constant.baseURL + "/comment/listComment", pagination)
          .then((res) => {
            if (!this.$common.isEmpty(res.data) && !this.$common.isEmpty(res.data.records)) {
              item.childComments = res.data;
              this.emoji(res.data.records, false);
              this.$nextTick(() => {
                this.$common.imgShow("#comment-content .pictureReg");
                this.artLoadMoreChildren(item);
              });
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      handleClose() {
        this.replyDialogVisible = false;
        this.floorComment = {};
        this.replyComment = {};
      }
    }
  }
</script>

<style scoped>

  /* ===== 评论输入区：留言规则卡 + 留言输入栏（与留言页视觉一致） ===== */
  /* 合并外框：规则卡、输入栏、标题、列表同属一张卡，只在外层留一圈描边与上下大圆角 */
  .art-comment-card {
    width: 100%;
    margin-bottom: 30px;
    border: 1px solid #000;
    border-radius: 18px;
    background: transparent;
    overflow: hidden;
  }

  html.dark .art-comment-card {
    /* 纯黑描边在深色页面底上不可见，改用 38% 的浅灰描边 */
    border-color: rgba(230, 230, 230, 0.38);
  }

  /* 输入区：外框已交给 .art-comment-card，自身不再描边、不再留外边距 */
  .art-comment-panel {
    width: 100%;
    background: transparent;
  }

  /* 留言规则：文字（标题 / 四条规则 / 图标里的「!」）统一用全站正文黑，
     字号、字重、行高与内边距与留言页保持一致 */
  .art-comment-rules {
    --rules-title: var(--fontColor);
    --rules-text: var(--fontColor);
    /* 规则图标：浅蓝圆角方形底板 + 白色气泡 + 蓝色横条，颜色均取自蓝色主题 */
    --rules-icon-bg: #5aa9f8;
    --rules-icon-line: #5aa9f8;
    --rules-icon-bubble: #ffffff;
    display: flex;
    align-items: flex-start;
    box-sizing: border-box;
    width: 100%;
    padding: 15px 20px;
    background: transparent;
  }

  html.dark .art-comment-rules {
    /* 深色主题正文亮色 #e6e6e6 */
    --rules-title: #e6e6e6;
    --rules-text: #e6e6e6;
    /* 深色下底板上提亮，保证白气泡仍清晰可辨 */
    --rules-icon-bg: #7cbcff;
    --rules-icon-line: #7cbcff;
    --rules-icon-bubble: #ffffff;
  }

  /* 图标容器尺寸、上边距与文字间距保持原样，仅把圆形「!」换成内联 SVG 气泡图标 */
  .art-comment-rules-icon {
    flex-shrink: 0;
    width: 20px;
    height: 20px;
    margin-top: 1px;
    border-radius: 6px;
    background: var(--rules-icon-bg);
    color: var(--rules-icon-line);
    user-select: none;
  }

  .art-comment-rules-icon svg {
    display: block;
    width: 100%;
    height: 100%;
  }

  .art-comment-rules-body {
    min-width: 0;
    margin-left: 10px;
  }

  .art-comment-rules-title {
    color: var(--rules-title);
    font-size: 17px;
    font-weight: 700;
    line-height: 22px;
  }

  .art-comment-rules-list {
    margin: 6px 0 0;
    padding: 0;
    list-style: none;
    color: var(--rules-text);
    font-size: 14.5px;
    line-height: 1.8;
  }

  /* 留言输入栏：底色交给共用外框（透明），只用一条细分隔线与规则卡分层 */
  .art-comment-bar {
    --bar-login: #0db7be;
    --bar-avatar: #0f9f91;
    --bar-count: #64748b;
    display: flex;
    align-items: center;
    gap: 12px;
    box-sizing: border-box;
    width: 100%;
    /* 骑线标签盒上沿在 field 顶边之上约 6.4px，14px 内边距仍留 7.6px 净空，不压顶边 */
    padding: 14px 18px;
    border-top: 1px solid rgba(0, 0, 0, 0.12);
    background: transparent;
  }

  html.dark .art-comment-bar {
    --bar-count: #a8c4bf;
    border-top-color: rgba(255, 255, 255, 0.12);
  }

  .art-comment-bar-avatar {
    flex-shrink: 0;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    overflow: hidden;
    background: var(--bar-avatar);
    color: #fff;
    font-size: 13px;
    line-height: 36px;
    text-align: center;
    user-select: none;
  }

  .art-comment-bar-avatar img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  /* 输入框容器：作为表情按钮的定位参照 */
  .art-comment-bar-input-wrap {
    position: relative;
    display: flex;
    flex: 1 1 0;
    min-width: 0;
  }

  /* 表情按钮：嵌在输入胶囊内部右端，透明底 + 中性灰实线，hover 转虚线 */
  .art-comment-bar-emoji {
    position: absolute;
    top: 50%;
    right: 5px;
    transform: translateY(-50%);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    padding: 0;
    border: 1px solid rgba(120, 120, 120, 0.16);
    border-radius: 50%;
    background: transparent;
    color: var(--themeBackground);
    font-size: 17px;
    line-height: 1;
    cursor: pointer;
    box-shadow:
      0 2px 5px rgba(38, 38, 38, 0.07),
      0 1px 2px rgba(38, 38, 38, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 0.8),
      inset 0 -1px 0 rgba(112, 112, 112, 0.1);
    transition: color 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background-color 0.22s ease;
  }

  .art-comment-bar-emoji:hover {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    background: transparent;
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
  }

  .art-comment-bar-emoji:active {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
    scale: 0.95;
  }

  html.dark .art-comment-bar-emoji {
    border-color: rgba(255, 255, 255, 0.16);
    color: var(--themeBackground);
    box-shadow:
      0 2px 5px rgba(0, 0, 0, 0.3),
      0 1px 2px rgba(0, 0, 0, 0.24),
      inset 0 1px 0 rgba(255, 255, 255, 0.06),
      inset 0 -1px 0 rgba(0, 0, 0, 0.3);
  }

  html.dark .art-comment-bar-emoji:hover,
  html.dark .art-comment-bar-emoji:active {
    border-color: rgba(255, 255, 255, 0.4);
    box-shadow:
      0 3px 6px rgba(0, 0, 0, 0.38),
      0 1px 2px rgba(0, 0, 0, 0.28),
      inset 0 1px 0 rgba(255, 255, 255, 0.1),
      inset 0 -1px 0 rgba(0, 0, 0, 0.34);
  }

  .art-comment-bar-input {
    display: block;
    box-sizing: border-box;
    width: 100%;
    height: 38px;
    /* 左侧留出半圆端头空间，文字不贴圆弧；右侧留出表情按钮宽度 + 间距 */
    padding: 0 42px 0 26px;
    border: none;
    border-radius: 999px;
    outline: none;
    background: #fff;
    color: #333;
    font-size: 14px;
  }

  /* 浮动标签：同一套 FloatInput，按留言输入栏的胶囊规格覆盖变量 */
  .art-comment-bar-input-wrap :deep(.float-field) {
    flex: 1 1 0;
    min-width: 0;
    --float-radius: 999px;
    /* 缺口比标签盒左右各外扩 2px，起点后移 2px 保持实际开口不压到左侧半圆 */
    --float-notch-left: 22px;
    --float-border-color: rgba(17, 48, 44, 0.12);
    --float-label-color: #757575;
    --float-label-active-color: #333;
    --float-accent-color: #0db7be;
    --float-margin-bottom: 0;
  }

  html.dark .art-comment-bar-input-wrap :deep(.float-field) {
    /* 胶囊输入框在深色下仍是白底，用深色 alpha 才能看见静止态描边 */
    --float-border-color: rgba(17, 48, 44, 0.16);
    /* 上浮后标签落在深色输入栏底上，需要浅色文字与强调色 */
    --float-label-active-color: #e2e8f0;
    --float-accent-color: #7fe3d8;
  }

  /* 上浮标签：胶囊是白底实心块，标签铺一块同色白底，成为胶囊顶边向上延伸的一小截 */
  .art-comment-bar-input-wrap :deep(.float-field:focus-within .float-label),
  .art-comment-bar-input-wrap :deep(.float-field.is-filled .float-label) {
    background: #fff;
    border-radius: 6px 6px 0 0;
    color: #4b5563;
  }

  .art-comment-bar-actions {
    display: flex;
    align-items: center;
    flex-shrink: 0;
    gap: 12px;
  }

  /* 未登录「登录」：透明底 + 中性灰实线 */
  .art-comment-bar-login {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    height: 34px;
    padding: 6px 12px;
    border: 1px solid rgba(120, 120, 120, 0.16);
    border-radius: 10px;
    background: transparent;
    color: var(--themeBackground);
    font-size: 14px;
    font-weight: 500;
    line-height: 20px;
    cursor: pointer;
    user-select: none;
    box-shadow:
      0 2px 5px rgba(38, 38, 38, 0.07),
      0 1px 2px rgba(38, 38, 38, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 0.8),
      inset 0 -1px 0 rgba(112, 112, 112, 0.1);
    transition: color 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background-color 0.22s ease;
  }

  .art-comment-bar-login:hover {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    color: var(--themeBackground);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
  }

  .art-comment-bar-login:active {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
    scale: 0.95;
  }

  html.dark .art-comment-bar-login {
    border-color: rgba(255, 255, 255, 0.16);
    color: var(--themeBackground);
    box-shadow:
      0 2px 5px rgba(0, 0, 0, 0.3),
      0 1px 2px rgba(0, 0, 0, 0.24),
      inset 0 1px 0 rgba(255, 255, 255, 0.06),
      inset 0 -1px 0 rgba(0, 0, 0, 0.3);
  }

  html.dark .art-comment-bar-login:hover,
  html.dark .art-comment-bar-login:active {
    border-color: rgba(255, 255, 255, 0.4);
    color: var(--themeBackground);
    box-shadow:
      0 3px 6px rgba(0, 0, 0, 0.38),
      0 1px 2px rgba(0, 0, 0, 0.28),
      inset 0 1px 0 rgba(255, 255, 255, 0.1),
      inset 0 -1px 0 rgba(0, 0, 0, 0.34);
  }

  .art-comment-bar-count {
    color: var(--bar-count);
    font-size: 12px;
    white-space: nowrap;
    user-select: none;
  }

  /* 「发送」：透明底 + 中性灰实线 + 蓝字（hover 转虚线） */
  .art-comment-bar-publish {
    box-sizing: border-box;
    height: 34px;
    padding: 6px 12px;
    border: 1px solid rgba(120, 120, 120, 0.16);
    border-radius: 10px;
    background: transparent;
    box-shadow:
      0 2px 5px rgba(38, 38, 38, 0.07),
      0 1px 2px rgba(38, 38, 38, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 0.8),
      inset 0 -1px 0 rgba(112, 112, 112, 0.1);
    color: var(--themeBackground);
    font-size: 14px;
    font-weight: 500;
    line-height: 20px;
    cursor: pointer;
    user-select: none;
    transition: color 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background-color 0.22s ease;
  }

  .art-comment-bar-publish:hover:not(:disabled) {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    background: transparent;
    color: var(--themeBackground);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
  }

  .art-comment-bar-publish:active:not(:disabled) {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
    scale: 0.95;
  }

  .art-comment-bar-publish:disabled {
    background: transparent;
    border-color: rgba(120, 120, 120, 0.16);
    color: var(--themeBackground);
    opacity: 0.55;
    box-shadow: none;
    cursor: not-allowed;
  }

  html.dark .art-comment-bar-publish {
    border-color: rgba(255, 255, 255, 0.16);
    color: var(--themeBackground);
    box-shadow:
      0 2px 5px rgba(0, 0, 0, 0.3),
      0 1px 2px rgba(0, 0, 0, 0.24),
      inset 0 1px 0 rgba(255, 255, 255, 0.06),
      inset 0 -1px 0 rgba(0, 0, 0, 0.3);
  }

  html.dark .art-comment-bar-publish:hover:not(:disabled),
  html.dark .art-comment-bar-publish:active:not(:disabled) {
    border-color: rgba(255, 255, 255, 0.4);
    color: var(--themeBackground);
    box-shadow:
      0 3px 6px rgba(0, 0, 0, 0.38),
      0 1px 2px rgba(0, 0, 0, 0.28),
      inset 0 1px 0 rgba(255, 255, 255, 0.1),
      inset 0 -1px 0 rgba(0, 0, 0, 0.34);
  }

  html.dark .art-comment-bar-publish:disabled {
    border-color: rgba(255, 255, 255, 0.16);
    color: var(--themeBackground);
  }

  @media screen and (max-width: 768px) {
    .art-comment-bar {
      flex-wrap: wrap;
      padding: 12px 14px;
      gap: 10px;
    }

    .art-comment-bar-input-wrap {
      flex: 1 1 calc(100% - 46px);
    }

    .art-comment-bar-actions {
      width: 100%;
      justify-content: flex-end;
    }

    .art-comment-rules {
      padding: 14px 16px;
    }

    .art-comment-rules-list {
      /* 窄屏再降半档，保证最长一条在 390 下整句单行不出现单字换行 */
      font-size: 13.5px;
      line-height: 1.75;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    .art-comment-bar-publish,
    .art-comment-bar-login,
    .art-comment-bar-emoji {
      transition: none;
    }

    .art-comment-bar-publish:active:not(:disabled),
    .art-comment-bar-login:active,
    .art-comment-bar-emoji:active {
      scale: 1;
    }
  }

  .commentInfo-title {
    margin-bottom: 20px;
    color: var(--greyFont);
    user-select: none;
  }

  /* 旧评论区（整片模式用同一 id 仅作滚动锚点，不套用此分隔与间距） */
  #comment-content:not(.msg-card-list) {
    border-bottom: 1px solid rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;
  }

  .commentInfo-detail {
    display: flex;
  }

  .commentInfo-avatar {
    border-radius: 5px;
  }

  .commentInfo-avatar-wrap {
    position: relative;
    flex-shrink: 0;
  }

  .commentInfo-badge {
    position: absolute;
    right: -3px;
    bottom: -3px;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: #2ed573;
    border: 2px solid #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.15);
  }

  .commentInfo-username {
    color: var(--orangeRed);
    font-size: 16px;
    font-weight: 600;
    margin-right: 5px;
  }

  .commentInfo-username-small {
    color: var(--orangeRed);
    font-size: 14px;
    font-weight: 600;
    margin-right: 5px;
  }

  .commentInfo-master {
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 3px;
    padding: 1px 7px;
    border: 1px solid rgba(230, 0, 18, 0.52);
    border-radius: 4px;
    overflow: hidden;
    background: rgba(230, 0, 18, 0.18);
    color: rgb(230, 0, 18);
    font-size: 12px;
    font-weight: 600;
    line-height: 16px;
    user-select: none;
    box-shadow: 0 1px 3px rgba(230, 0, 18, 0.16);
  }

  .commentInfo-site-badge {
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 3px;
    padding: 1px 7px;
    border: 1px solid rgba(230, 0, 18, 0.46);
    border-radius: 4px;
    overflow: hidden;
    background: rgba(230, 0, 18, 0.12);
    color: rgb(230, 0, 18);
    font-size: 12px;
    font-weight: 600;
    line-height: 16px;
    user-select: none;
    box-shadow: 0 1px 3px rgba(230, 0, 18, 0.12);
  }

  .commentInfo-other {
    font-size: 12px;
    color: var(--greyFont);
    user-select: none;
  }

  /* 文章评论区：与留言页同款描边小按钮（1px 浅描边 + 圆角 10 + 蓝字） */
  .commentInfo-reply-btn {
    display: inline-flex;
    align-items: center;
    padding: 3px 10px;
    border: 1px solid rgba(120, 120, 120, 0.16);
    border-radius: 10px;
    background: transparent;
    color: var(--themeBackground);
    font-size: 12px;
    font-weight: 500;
    line-height: 18px;
    cursor: pointer;
    user-select: none;
    transition: border-color 0.22s ease, color 0.22s ease;
  }

  .commentInfo-reply-btn:hover {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
  }

  .commentInfo-reply-btn:active {
    scale: 0.95;
  }

  @media (prefers-reduced-motion: reduce) {
    .commentInfo-reply-btn {
      transition: none;
    }

    .commentInfo-reply-btn:active {
      scale: 1;
    }
  }

  /* 文章评论列表：透明底卡片，仅用 1px 分隔线区分，与留言页保持一致 */
  .commentInfo-list .commentInfo-detail {
    padding: 16px 0;
    border-bottom: 1px solid rgba(17, 48, 44, 0.07);
  }

  .commentInfo-list .commentInfo-detail:last-child {
    border-bottom: 0;
  }

  .commentInfo-list .commentInfo-detail .commentInfo-detail {
    padding: 10px 0 0;
    border-bottom: 0;
  }

  .commentInfo-list .commentInfo-content {
    margin: 10px 0 12px;
    padding: 0;
    background: transparent;
    border-radius: 0;
  }

  .commentInfo-list > .msg-child-toggle {
    margin: 14px auto 16px;
  }

  .commentInfo-content {
    margin: 15px 0 25px;
    padding: 18px 20px;
    background: var(--commentContent);
    border-radius: 12px;
    color: var(--fontColor);
    word-break: break-word;
  }

  .pagination-wrap {
    display: flex;
    justify-content: center;
    margin-bottom: 10px;
  }

  .pagination {
    padding: 6px 20px;
    border: 1px solid var(--lightGray);
    border-radius: 3rem;
    color: var(--greyFont);
    user-select: none;
    cursor: pointer;
    text-align: center;
    font-size: 12px;
  }

  .pagination:hover {
    border: 1px solid var(--themeBackground);
    color: var(--themeBackground);
    box-shadow: 0 0 5px var(--themeBackground);
  }
  /* ===== 留言页卡片式列表（仅 cardList=true 时渲染的结构使用） ===== */
  .msg-list-title {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    user-select: none;
  }

  .msg-list-title-main {
    color: #0f766e;
    font-size: 17px;
    font-weight: 700;
  }

  .msg-list-title-divider {
    width: 1px;
    height: 14px;
    margin: 0 10px;
    background: #cbe9e3;
  }

  .msg-list-title-sub {
    color: #94a3b8;
    font-size: 13px;
  }

  /* 整片留言列表：只在外层保留上下圆角与 1px 描边 */
  .msg-card-list {
    box-sizing: border-box;
    width: 100%;
    border: 1px solid #eef7f5;
    border-radius: 14px;
    background: #fbfefe;
    overflow: hidden;
  }

  /* 单条留言：去掉外边距与自身圆角/描边，仅用 1px 分隔线分隔 */
  .msg-card {
    box-sizing: border-box;
    width: 100%;
    margin-bottom: 0;
    padding: 16px 18px;
    border: 0;
    border-bottom: 1px solid rgba(17, 48, 44, 0.07);
    border-radius: 0;
    background: transparent;
  }

  .msg-card:last-child {
    border-bottom: 0;
  }

  .msg-card-head {
    display: flex;
    align-items: flex-start;
    flex-wrap: wrap;
    gap: 10px;
  }

  .msg-card-avatar-wrap {
    position: relative;
    flex-shrink: 0;
  }

  .msg-card-avatar {
    flex-shrink: 0;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    overflow: hidden;
    background: #e5e7eb;
    color: #94a3b8;
    font-size: 14px;
    line-height: 36px;
    text-align: center;
    user-select: none;
  }

  .msg-card-avatar img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  /* 实名用户认证角标：头像右下角绿色圆 + 白色对勾 */
  .msg-card-badge {
    position: absolute;
    right: -2px;
    bottom: -2px;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: #2ed573;
    border: 2px solid #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.15);
  }

  .msg-card-meta {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    flex: 1 1 0;
    min-width: 0;
    padding-top: 8px;
  }

  .msg-card-name {
    color: #0f766e;
    font-size: 14px;
    font-weight: 700;
    word-break: break-all;
  }

  .msg-card-boss {
    --badge-sheen: rgba(255, 255, 255, 0.62);
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 3px;
    padding: 1px 7px;
    border: 1px solid rgba(230, 0, 18, 0.52);
    border-radius: 4px;
    overflow: hidden;
    background: rgba(230, 0, 18, 0.18);
    color: rgb(230, 0, 18);
    font-size: 11px;
    font-weight: 600;
    line-height: 16px;
    user-select: none;
    box-shadow: 0 1px 3px rgba(230, 0, 18, 0.16);
  }

  /* 角色徽章流光效果：白色高光从左滑到右 */
  .commentInfo-master::before,
  .commentInfo-site-badge::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 50%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
    animation: badge-shine 3s ease-in-out infinite;
    pointer-events: none;
  }

  @keyframes badge-shine {
    to {
      left: 150%;
    }
  }

  /* 站点名称徽章：与主理人徽章同属主理人色系，底色与描边浅一档 */
  .msg-site-badge {
    --badge-sheen: rgba(255, 255, 255, 0.62);
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 3px;
    padding: 1px 7px;
    border: 1px solid rgba(230, 0, 18, 0.46);
    border-radius: 4px;
    overflow: hidden;
    background: rgba(230, 0, 18, 0.12);
    color: rgb(230, 0, 18);
    font-size: 11px;
    font-weight: 600;
    line-height: 16px;
    user-select: none;
    box-shadow: 0 1px 3px rgba(230, 0, 18, 0.12);
  }

  /* 正式会员徽章：沿用普通友链档（tier-0）中性配色，规格与主理人徽章保持一致 */
  .msg-card-member {
    --badge-sheen: rgb(198, 207, 215);
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 3px;
    padding: 1px 7px;
    border: 1px solid rgba(90, 106, 122, 0.46);
    border-radius: 4px;
    overflow: hidden;
    background: rgba(90, 106, 122, 0.14);
    color: rgb(54, 72, 88);
    font-size: 11px;
    font-weight: 600;
    line-height: 16px;
    user-select: none;
    box-shadow: 0 1px 3px rgba(54, 72, 88, 0.12);
  }

  .msg-card-member svg {
    color: rgb(54, 72, 88);
    filter: drop-shadow(0 0 1.92px rgba(54, 72, 88, 0.2));
  }

  /* 作者徽章：沿用优秀友链档（tier-3）蓝色配色，固定宽度与主理人徽章对齐 */
  .msg-card-author {
    --badge-sheen: rgb(150, 211, 255);
    position: relative;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 2px;
    width: 63px;
    padding: 1px 0;
    border: 1px solid color-mix(in srgb, var(--themeBackground) 46%, transparent);
    border-radius: 4px;
    overflow: hidden;
    background: color-mix(in srgb, var(--themeBackground) 14%, transparent);
    color: rgb(23, 139, 192);
    font-size: 11px;
    font-weight: 600;
    line-height: 16px;
    white-space: nowrap;
    user-select: none;
    box-shadow: 0 1px 3px rgba(23, 139, 192, 0.12);
  }

  .msg-card-author svg {
    color: rgb(23, 139, 192);
    filter: drop-shadow(0 0 1.92px rgba(23, 139, 192, 0.2));
  }

  /* 管理员徽章：沿用推荐友链档（tier-1）金色配色 */
  .msg-card-admin {
    --badge-sheen: rgb(255, 224, 138);
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 3px;
    padding: 1px 7px;
    border: 1px solid rgba(232, 173, 37, 0.46);
    border-radius: 4px;
    overflow: hidden;
    background: rgba(232, 173, 37, 0.14);
    color: rgb(150, 112, 20);
    font-size: 11px;
    font-weight: 600;
    line-height: 16px;
    user-select: none;
    box-shadow: 0 1px 3px rgba(150, 112, 20, 0.12);
  }

  .msg-card-admin svg {
    color: rgb(150, 112, 20);
    filter: drop-shadow(0 0 1.92px rgba(150, 112, 20, 0.2));
  }

  /* 图标跟随主理人色系，与友链主理人徽标同源 */
  .msg-card-boss svg,
  .msg-site-badge svg,
  .commentInfo-master svg,
  .commentInfo-site-badge svg {
    color: rgb(230, 0, 18);
    filter: drop-shadow(0 0 1.92px rgba(230, 0, 18, 0.25));
  }

  /* 主理人留言的用户名跟随友链主理人标题色 */
  .msg-card-meta:has(.msg-card-boss) .msg-card-name,
  .commentInfo-detail:has(.commentInfo-master) .commentInfo-username,
  .commentInfo-detail:has(.commentInfo-master) .commentInfo-username-small {
    color: rgb(230, 0, 18);
  }

  /* 主理人用户名流光：高光从左向右扫过文字，与徽标扫光同节奏 */
  .msg-card-meta:has(.msg-card-boss) .msg-card-name,
  .commentInfo-detail:has(.commentInfo-master) .commentInfo-username,
  .commentInfo-detail:has(.commentInfo-master) .commentInfo-username-small {
    color: transparent;
    -webkit-text-fill-color: transparent;
    background-image: linear-gradient(100deg,
      rgb(230, 0, 18) 0%,
      rgb(230, 0, 18) 42%,
      rgb(255, 255, 255) 50%,
      rgb(230, 0, 18) 58%,
      rgb(230, 0, 18) 100%);
    background-size: 250% 100%;
    background-repeat: no-repeat;
    background-position: 100% 0;
    -webkit-background-clip: text;
    background-clip: text;
    animation: name-shimmer 3s linear infinite;
  }

  /* 位移限定在 0%–100%，保证文字始终被渐变覆盖，不会出现局部透明 */
  @keyframes name-shimmer {
    0% {
      background-position: 100% 0;
    }
    100% {
      background-position: 0% 0;
    }
  }

  /* 管理员 / 作者 / 正式会员用户名跟随各自徽章配色，参数与主理人完全一致（100deg、250%、3s 线性循环） */
  .msg-card-meta:has(.msg-card-author) .msg-card-name,
  .msg-card-meta:has(.msg-card-admin) .msg-card-name,
  .msg-card-meta:has(.msg-card-member) .msg-card-name {
    color: transparent;
    -webkit-text-fill-color: transparent;
    background-size: 250% 100%;
    background-repeat: no-repeat;
    background-position: 100% 0;
    -webkit-background-clip: text;
    background-clip: text;
    animation: name-shimmer 3s linear infinite;
  }

  .msg-card-meta:has(.msg-card-author) .msg-card-name {
    background-image: linear-gradient(100deg,
      rgb(23, 139, 192) 0%,
      rgb(23, 139, 192) 42%,
      rgb(190, 226, 255) 50%,
      rgb(23, 139, 192) 58%,
      rgb(23, 139, 192) 100%);
  }

  .msg-card-meta:has(.msg-card-admin) .msg-card-name {
    background-image: linear-gradient(100deg,
      rgb(150, 112, 20) 0%,
      rgb(150, 112, 20) 42%,
      rgb(255, 224, 138) 50%,
      rgb(150, 112, 20) 58%,
      rgb(150, 112, 20) 100%);
  }

  .msg-card-meta:has(.msg-card-member) .msg-card-name {
    background-image: linear-gradient(100deg,
      rgb(54, 72, 88) 0%,
      rgb(54, 72, 88) 42%,
      rgb(198, 207, 215) 50%,
      rgb(54, 72, 88) 58%,
      rgb(54, 72, 88) 100%);
  }

  /* 置顶标识与站长专属操作入口 */
  .msg-card-top-tag {
    padding: 1px 7px;
    border-radius: 4px;
    background: #ef4444;
    color: #fff;
    font-size: 11px;
    font-weight: 600;
    line-height: 16px;
    user-select: none;
  }

  .msg-card-top-btn {
    padding: 5px 12px;
    border: 1px dashed rgba(15, 118, 110, 0.42);
    border-radius: var(--ui-btn-radius);
    background: var(--ui-btn-bg);
    box-shadow: var(--ui-btn-shadow);
    color: #0f766e;
    font-size: 13px;
    font-weight: 500;
    line-height: 18px;
    cursor: pointer;
    user-select: none;
    transition: var(--ui-btn-transition);
  }

  .msg-card-top-btn:hover {
    border-style: dashed;
    border-color: rgba(15, 118, 110, 0.55);
    background: var(--ui-btn-bg);
    box-shadow: var(--ui-btn-shadow-hover);
  }

  .msg-card-top-btn:active {
    border-style: dashed;
    border-color: rgba(15, 118, 110, 0.55);
    box-shadow: var(--ui-btn-shadow-hover);
    scale: 0.95;
  }

  .msg-card-loc {
    display: inline-flex;
    align-items: center;
    gap: 3px;
    padding: 1px 8px;
    border: 1px solid #cbe9e3;
    border-radius: 999px;
    background: #e2f3f2;
    color: #0f766e;
    font-size: 12px;
    line-height: 18px;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .msg-card-date {
    color: #64748b;
    font-size: 12px;
    user-select: none;
  }

  .msg-card-actions {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-left: auto;
    padding-top: 6px;
    flex-shrink: 0;
  }

  .msg-card-reply-count {
    padding: 2px 10px;
    border-radius: 999px;
    background: #fef3c7;
    color: #b45309;
    font-size: 12px;
    line-height: 18px;
    user-select: none;
  }

  .msg-card-reply-btn {
    padding: 5px 12px;
    border: 1px dashed rgba(180, 83, 9, 0.4);
    border-radius: var(--ui-btn-radius);
    background: var(--ui-btn-bg);
    box-shadow: var(--ui-btn-shadow);
    color: #b45309;
    font-size: 13px;
    font-weight: 500;
    line-height: 18px;
    cursor: pointer;
    user-select: none;
    transition: var(--ui-btn-transition);
  }

  .msg-card-reply-btn:hover {
    border-style: dashed;
    border-color: rgba(180, 83, 9, 0.55);
    background: var(--ui-btn-bg);
    box-shadow: var(--ui-btn-shadow-hover);
  }

  .msg-card-reply-btn:active {
    border-style: dashed;
    border-color: rgba(180, 83, 9, 0.55);
    box-shadow: var(--ui-btn-shadow-hover);
    scale: 0.95;
  }

  .msg-card-body {
    margin: 10px 0 0 46px;
    color: #334155;
    font-size: 14px;
    line-height: 1.9;
    word-break: break-word;
  }

  .msg-card-body img {
    max-width: 100%;
  }

  .msg-child-list {
    margin: 14px 0 0 46px;
  }

  .msg-child {
    padding: 12px 14px;
    margin-bottom: 10px;
    border: 1px solid #eef4f2;
    border-radius: 10px;
    background: #fdfefe;
  }

  .msg-child .msg-card-head {
    align-items: center;
  }

  .msg-child .msg-card-meta {
    padding-top: 0;
  }

  .msg-child-avatar {
    width: 28px;
    height: 28px;
    font-size: 12px;
    line-height: 28px;
  }

  .msg-child .msg-card-actions {
    padding-top: 0;
  }

  .msg-child-body {
    margin: 8px 0 0 38px;
    font-size: 13px;
  }

  .msg-child-at {
    margin-right: 4px;
    color: #0f766e;
    font-weight: 600;
  }

  /* 楼中楼折叠入口：一行蓝色小字，不引入新按钮样式，避免覆盖按钮统一改造 */
  .msg-child-toggle {
    width: fit-content;
    margin: 2px auto 0;
    padding: 2px 6px;
    color: var(--themeBackground);
    font-size: 12px;
    line-height: 18px;
    cursor: pointer;
    user-select: none;
  }

  .msg-child-toggle:hover {
    color: var(--themeBackgroundDeep);
  }

  .msg-card-more {
    width: fit-content;
    margin: 2px auto 0;
    padding: 3px 16px;
    border: 1px dashed #cfe8e2;
    border-radius: 999px;
    color: #0f766e;
    font-size: 12px;
    cursor: pointer;
    user-select: none;
  }

  /* 行内回复输入条：输入框 + 发送 + 取消排成一行。
     外框（1px 描边）只圈住输入框本身，不再把「发送」框进去：
     边框由 FloatInput 自绘，标签上浮时骑在输入框上边线上，clip-path 真正开缺口。 */
  .msg-inline-reply {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 12px 0 2px 46px;
  }

  .msg-inline-input {
    display: block;
    box-sizing: border-box;
    width: 100%;
    height: 30px;
    /* 边框由 FloatInput 自绘，输入框自身不再有圆角与描边 */
    padding: 0 12px;
    border: none;
    border-radius: 0;
    outline: none;
    background: transparent;
    color: #4b5563;
    font-size: 13px;
  }

  /* 输入框外框即 FloatInput 的字段，按行内回复条的规格覆盖变量 */
  .msg-inline-reply :deep(.float-field) {
    flex: 1 1 auto;
    min-width: 0;
    --float-radius: 10px;
    /* 缺口起点 = 14 - 2 = 12px，避开 10px 圆角，不会切到圆弧 */
    --float-notch-left: 14px;
    --float-label-size: 13px;
    --float-border-color: rgba(120, 120, 120, 0.16);
    --float-accent-color: color-mix(in srgb, var(--themeBackground) 50%, transparent);
    --float-label-color: #9ca3af;
    --float-label-active-color: #4b5563;
    --float-margin-bottom: 0;
  }

  /* 发送 / 取消：同一套描边外观（透明底、蓝字、柔光），均与输入框外框同高 */
  .msg-inline-send,
  .msg-inline-cancel {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    /* 20 行高 + 上下各 4px + 2px 描边 = 30px，与输入框外框平齐 */
    padding: 4px 12px;
    border: 1px solid rgba(120, 120, 120, 0.16);
    border-radius: 10px;
    background: transparent;
    color: var(--themeBackground);
    font-size: 13px;
    font-weight: 500;
    line-height: 20px;
    cursor: pointer;
    user-select: none;
    box-shadow:
      0 2px 5px rgba(38, 38, 38, 0.07),
      0 1px 2px rgba(38, 38, 38, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 0.8),
      inset 0 -1px 0 rgba(112, 112, 112, 0.1);
    transition: color 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
  }

  .msg-inline-send:hover,
  .msg-inline-cancel:hover {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
  }

  .msg-inline-send:active,
  .msg-inline-cancel:active {
    scale: 0.95;
  }

  @media (prefers-reduced-motion: reduce) {
    .msg-inline-send,
    .msg-inline-cancel {
      transition: none;
    }

    .msg-inline-send:active,
    .msg-inline-cancel:active {
      scale: 1;
    }
  }

  html.dark .msg-list-title-main {
    color: #5eead4;
  }

  html.dark .msg-list-title-divider {
    background: #334155;
  }

  html.dark .msg-list-title-sub {
    color: #94a3b8;
  }

  html.dark .msg-card-list {
    border-color: #223833;
    background: #14211e;
  }

  html.dark .msg-card {
    border-bottom-color: rgba(255, 255, 255, 0.08);
  }

  html.dark .msg-card-name {
    color: #5eead4;
  }

  /* 深色主理人色系：提亮正红，与友链主理人档一致 */
  html.dark .msg-card-boss,
  html.dark .commentInfo-master {
    border-color: rgba(255, 120, 130, 0.52);
    background: rgba(255, 120, 130, 0.18);
    color: rgb(255, 120, 130);
    box-shadow: 0 1px 3px rgba(255, 59, 74, 0.24);
  }

  html.dark .msg-site-badge,
  html.dark .commentInfo-site-badge {
    border-color: rgba(255, 59, 74, 0.46);
    background: rgba(255, 59, 74, 0.14);
    color: rgb(255, 120, 130);
    box-shadow: 0 1px 3px rgba(255, 59, 74, 0.18);
  }

  /* 深色正式会员徽章：取友链普通档深色中性配色 */
  html.dark .msg-card-member {
    --badge-sheen: rgb(188, 197, 206);
    border-color: rgba(170, 178, 186, 0.46);
    background: rgba(170, 178, 186, 0.14);
    color: rgb(230, 232, 234);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.18);
  }

  html.dark .msg-card-member svg {
    color: rgb(230, 232, 234);
    filter: drop-shadow(0 0 1.92px rgba(230, 232, 234, 0.2));
  }

  /* 深色作者徽章：取友链优秀档深色蓝色配色 */
  html.dark .msg-card-author {
    --badge-sheen: rgb(214, 240, 255);
    border-color: rgba(99, 179, 255, 0.46);
    background: rgba(99, 179, 255, 0.14);
    color: rgb(147, 205, 255);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.18);
  }

  html.dark .msg-card-author svg {
    color: rgb(147, 205, 255);
    filter: drop-shadow(0 0 1.92px rgba(147, 205, 255, 0.2));
  }

  /* 深色管理员徽章：取友链推荐档深色金色配色 */
  html.dark .msg-card-admin {
    --badge-sheen: rgb(255, 236, 186);
    border-color: rgba(232, 173, 37, 0.46);
    background: rgba(232, 173, 37, 0.14);
    color: rgb(240, 210, 140);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.18);
  }

  html.dark .msg-card-admin svg {
    color: rgb(240, 210, 140);
    filter: drop-shadow(0 0 1.92px rgba(240, 210, 140, 0.2));
  }

  html.dark .msg-card-boss svg,
  html.dark .msg-site-badge svg,
  html.dark .commentInfo-master svg,
  html.dark .commentInfo-site-badge svg {
    color: rgb(255, 120, 130);
    filter: drop-shadow(0 0 1.92px rgba(255, 120, 130, 0.25));
  }

  html.dark .msg-card-meta:has(.msg-card-boss) .msg-card-name,
  html.dark .commentInfo-detail:has(.commentInfo-master) .commentInfo-username,
  html.dark .commentInfo-detail:has(.commentInfo-master) .commentInfo-username-small {
    color: rgb(255, 120, 130);
  }

  /* 深色流光：基底提亮红，高光取友链深色 shimmer 值 */
  html.dark .msg-card-meta:has(.msg-card-boss) .msg-card-name,
  html.dark .commentInfo-detail:has(.commentInfo-master) .commentInfo-username,
  html.dark .commentInfo-detail:has(.commentInfo-master) .commentInfo-username-small {
    color: transparent;
    -webkit-text-fill-color: transparent;
    background-image: linear-gradient(100deg,
      rgb(255, 120, 130) 0%,
      rgb(255, 120, 130) 42%,
      rgb(255, 255, 255) 50%,
      rgb(255, 120, 130) 58%,
      rgb(255, 120, 130) 100%);
  }

  /* 深色角色用户名流光：基底按各档提亮，高光取白色，参数与主理人一致 */
  html.dark .msg-card-meta:has(.msg-card-author) .msg-card-name {
    color: transparent;
    -webkit-text-fill-color: transparent;
    background-image: linear-gradient(100deg,
      rgb(147, 205, 255) 0%,
      rgb(147, 205, 255) 42%,
      rgb(255, 255, 255) 50%,
      rgb(147, 205, 255) 58%,
      rgb(147, 205, 255) 100%);
  }

  html.dark .msg-card-meta:has(.msg-card-admin) .msg-card-name {
    color: transparent;
    -webkit-text-fill-color: transparent;
    background-image: linear-gradient(100deg,
      rgb(240, 210, 140) 0%,
      rgb(240, 210, 140) 42%,
      rgb(255, 255, 255) 50%,
      rgb(240, 210, 140) 58%,
      rgb(240, 210, 140) 100%);
  }

  html.dark .msg-card-meta:has(.msg-card-member) .msg-card-name {
    color: transparent;
    -webkit-text-fill-color: transparent;
    background-image: linear-gradient(100deg,
      rgb(230, 232, 234) 0%,
      rgb(230, 232, 234) 42%,
      rgb(255, 255, 255) 50%,
      rgb(230, 232, 234) 58%,
      rgb(230, 232, 234) 100%);
  }

  html.dark .msg-card-top-tag {
    background: #dc2626;
    color: #fff;
  }

  html.dark .msg-card-top-btn {
    border-color: rgba(20, 184, 166, 0.35);
    background: var(--ui-btn-bg);
    color: #7dd3c8;
  }

  html.dark .msg-card-top-btn:hover {
    background: var(--ui-btn-bg);
  }

  html.dark .msg-card-loc {
    border-color: #2f6b63;
    background: rgba(15, 118, 110, 0.18);
    color: #7dd3c8;
  }

  html.dark .msg-card-date {
    color: #94a3b8;
  }

  html.dark .msg-card-reply-count,
  html.dark .msg-card-reply-btn {
    border-color: rgba(253, 230, 138, 0.35);
    background: var(--ui-btn-bg);
    color: #fcd34d;
  }

  html.dark .msg-card-reply-btn:hover {
    background: var(--ui-btn-bg);
  }

  html.dark .msg-card-body {
    color: #cbd5e1;
  }

  html.dark .msg-child {
    border-color: #24382f;
    background: #1b2b27;
  }

  html.dark .msg-child-at {
    color: #5eead4;
  }

  html.dark .msg-child-toggle {
    color: var(--themeBackground);
  }

  html.dark .msg-child-toggle:hover {
    color: #a3d0ff;
  }

  html.dark .commentInfo-reply-btn {
    border-color: rgba(255, 255, 255, 0.16);
    color: var(--themeBackground);
  }

  html.dark .commentInfo-reply-btn:hover {
    border-color: rgba(255, 255, 255, 0.4);
    color: #a3d0ff;
  }

  html.dark .commentInfo-list .commentInfo-detail {
    border-bottom-color: rgba(255, 255, 255, 0.08);
  }

  html.dark .msg-card-more {
    border-color: #2f4a44;
    color: #7dd3c8;
  }

  html.dark .msg-inline-reply :deep(.float-field) {
    /* 外框由字段自绘，深色下换成白色 alpha 描边 */
    --float-border-color: rgba(255, 255, 255, 0.16);
    --float-accent-color: var(--themeBackground);
    --float-label-color: #94a3b8;
    --float-label-active-color: #e2e8f0;
  }

  html.dark .msg-inline-input {
    color: #e2e8f0;
  }

  html.dark .msg-inline-send,
  html.dark .msg-inline-cancel {
    border-color: rgba(255, 255, 255, 0.16);
    color: var(--themeBackground);
    box-shadow:
      0 2px 5px rgba(0, 0, 0, 0.3),
      0 1px 2px rgba(0, 0, 0, 0.24),
      inset 0 1px 0 rgba(255, 255, 255, 0.06),
      inset 0 -1px 0 rgba(0, 0, 0, 0.3);
  }

  html.dark .msg-inline-send:hover,
  html.dark .msg-inline-cancel:hover {
    border-color: rgba(255, 255, 255, 0.4);
    box-shadow:
      0 3px 6px rgba(0, 0, 0, 0.38),
      0 1px 2px rgba(0, 0, 0, 0.28),
      inset 0 1px 0 rgba(255, 255, 255, 0.1),
      inset 0 -1px 0 rgba(0, 0, 0, 0.34);
  }

  @media screen and (max-width: 600px) {
    .msg-card {
      padding: 14px;
    }

    .msg-card-body,
    .msg-child-list,
    .msg-inline-reply {
      margin-left: 0;
    }

    .msg-child-body {
      margin-left: 0;
    }

    /* 窄屏下输入占满整行，发送/取消换到下一行靠右 */
    .msg-inline-reply {
      flex-wrap: wrap;
      gap: 8px;
    }

    .msg-inline-reply :deep(.float-field) {
      flex: 1 1 100%;
    }

    .msg-inline-send {
      margin-left: auto;
    }
  }

  /* ===== 文章评论区：留言卡片的独立副本（仅 art-* 生效，与留言页样式互不影响） ===== */
  /* 评论段：并入同一张卡，透明底，只用一条细分隔线与输入栏分层 */
  .art-comment {
    box-sizing: border-box;
    width: 100%;
    padding: 16px 20px 20px;
    border-top: 1px solid rgba(0, 0, 0, 0.12);
    background: transparent;
  }

  html.dark .art-comment {
    border-top-color: rgba(255, 255, 255, 0.12);
  }

  /* 标题区与列表之间沿用同一条细分隔线；负外边距抵消左右内边距，让分隔线通栏铺满卡片 */
  .art-comment .art-list-title {
    margin-left: -20px;
    margin-right: -20px;
    padding: 0 20px 10px;
    border-bottom: 1px solid rgba(17, 48, 44, 0.07);
  }

  html.dark .art-comment .art-list-title {
    border-bottom-color: rgba(255, 255, 255, 0.08);
  }

  /* 空评论态：同样并入卡片，保留分隔线与底部留白 */
  .art-comment-card > .myCenter {
    box-sizing: border-box;
    padding: 20px;
    border-top: 1px solid rgba(0, 0, 0, 0.12);
  }

  html.dark .art-comment-card > .myCenter {
    border-top-color: rgba(255, 255, 255, 0.12);
  }

  .art-list-title {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    user-select: none;
  }

  /* 评论排序：胶囊式标签，靠右排列 */
  .art-sort {
    display: flex;
    align-items: center;
    gap: 6px;
    margin: 0;
    margin-left: auto;
    padding: 0;
    list-style: none;
  }

  .art-sort li {
    padding: 3px 12px;
    border: 1px solid rgba(120, 120, 120, 0.2);
    border-radius: 999px;
    background: transparent;
    color: #94a3b8;
    font-size: 12px;
    font-weight: 500;
    line-height: 1.6;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .art-sort li:hover {
    border-color: rgba(120, 120, 120, 0.4);
    color: #64748b;
  }

  .art-sort li.active {
    border-color: #0f766e;
    background: #0f766e;
    color: #fff;
  }

  .art-list-title-main {
    color: #0f766e;
    font-size: 17px;
    font-weight: 700;
  }

  .art-list-title-divider {
    width: 1px;
    height: 14px;
    margin: 0 10px;
    background: #cbe9e3;
  }

  .art-list-title-sub {
    color: #94a3b8;
    font-size: 13px;
  }

  /* 列表透明底、不画外框，卡片之间只用 1px 分隔线区分 */
  .art-card-list {
    box-sizing: border-box;
    width: 100%;
    background: transparent;
  }

  .art-card-item {
    position: relative;
    display: flex;
    padding: 0.5em 0;
  }

  .art-card {
    flex: 1;
    width: 0;
    min-width: 0;
    box-sizing: border-box;
    padding-bottom: 0.5em;
    border-bottom: 1px dashed rgba(0, 0, 0, 0.09);
    background: transparent;
  }

  .art-card-item:last-child .art-card {
    border-bottom: 0;
  }

  .art-card-head {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 0.5rem;
    line-height: 1.5;
  }

  /* 名字行与左侧头像垂直居中 */
  .art-card-item > .art-card > .art-card-head {
    padding-top: 0.5rem;
  }

  .art-child .art-card-head {
    padding-top: 0.2rem;
  }

  .art-user {
    position: relative;
    flex-shrink: 0;
    align-self: flex-start;
    margin-right: 0.75em;
  }

  .art-avatar {
    flex-shrink: 0;
    width: 3.25rem;
    height: 3.25rem;
    border-radius: 50%;
    overflow: hidden;
    background: #e5e7eb;
    color: #94a3b8;
    font-size: 14px;
    line-height: 3.25rem;
    text-align: center;
    user-select: none;
  }

  .art-avatar img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .art-meta {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 0.25em;
    margin-top: 0.35em;
    line-height: 1;
  }

  .art-top-tag {
    padding: 1px 8px;
    border-radius: 5px;
    background: #ef4444;
    color: #fff;
    font-size: 11px;
    font-weight: 600;
    line-height: 18px;
    user-select: none;
  }

  /* 管理员 / 作者 / 正式会员徽章流光：沿用主理人名字同款 115deg 遮罩扫光（mask-size 238%、mask-position 100%→0%、3s 线性循环），颜色由 --badge-sheen 提供 */
  /* 站点名称与主理人徽章共用同一套扫光，保证两枚徽章同时起扫、光带一次扫过整行 */
  .msg-card-member::after,
  .msg-card-author::after,
  .msg-card-admin::after,
  .msg-card-boss::after,
  .msg-site-badge::after {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: inherit;
    background: var(--badge-sheen);
    -webkit-mask-image: linear-gradient(115deg,
      transparent 42%,
      rgba(255, 255, 255, 0.5) 46%,
      #fff 48%,
      #fff 52%,
      rgba(255, 255, 255, 0.5) 54%,
      transparent 58%);
    mask-image: linear-gradient(115deg,
      transparent 42%,
      rgba(255, 255, 255, 0.5) 46%,
      #fff 48%,
      #fff 52%,
      rgba(255, 255, 255, 0.5) 54%,
      transparent 58%);
    -webkit-mask-size: 238% 100%;
    mask-size: 238% 100%;
    -webkit-mask-repeat: no-repeat;
    mask-repeat: no-repeat;
    -webkit-mask-position: 100% 0;
    mask-position: 100% 0;
    opacity: 0.55;
    pointer-events: none;
    animation: member-badge-shimmer 3s linear 0s infinite;
  }

  @keyframes member-badge-shimmer {
    0% {
      -webkit-mask-position: 100% 0;
      mask-position: 100% 0;
    }
    100% {
      -webkit-mask-position: 0% 0;
      mask-position: 0% 0;
    }
  }

  /* 深色模式：扫光颜色由徽章自身的 --badge-sheen 深色值提供 */
  @media (prefers-reduced-motion: reduce) {
    .msg-card-member::after,
    .msg-card-author::after,
    .msg-card-admin::after,
    .msg-card-boss::after,
    .msg-site-badge::after {
      animation: none;
      opacity: 0;
    }
  }

  .art-loc,
  .art-browser,
  .art-os {
    display: inline-block;
    max-width: 100%;
    padding: 2px 4px;
    border: 1px solid rgba(0, 0, 0, 0.06);
    border-radius: 0.2em;
    background: #f8f8f8;
    color: #999;
    font-size: 0.625em;
    line-height: 1.5;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .art-loc-icon {
    vertical-align: -1px;
    margin-right: 2px;
  }

  .art-date {
    color: #999;
    font-size: 0.75em;
    user-select: none;
  }

  .art-actions {
    display: flex;
    align-items: center;
    gap: 0.75em;
    margin-left: auto;
    flex-shrink: 0;
    line-height: 1;
  }

  .art-reply-count {
    color: #999;
    font-size: 0.75em;
    line-height: 1;
    user-select: none;
  }

  /* 置顶 / 回复：与「发送」按钮同款描边按钮 */
  .art-top-btn,
  .art-btn {
    box-sizing: border-box;
    display: inline-flex;
    align-items: center;
    height: 26px;
    padding: 0 10px;
    border: 1px solid rgba(120, 120, 120, 0.16);
    border-radius: 8px;
    background: transparent;
    box-shadow:
      0 2px 5px rgba(38, 38, 38, 0.07),
      0 1px 2px rgba(38, 38, 38, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 0.8),
      inset 0 -1px 0 rgba(112, 112, 112, 0.1);
    color: var(--themeBackground);
    font-size: 0.75em;
    font-weight: 500;
    line-height: 1;
    cursor: pointer;
    user-select: none;
    transition: border-color 0.2s ease, box-shadow 0.2s ease, scale 0.1s ease;
  }

  .art-top-btn:hover,
  .art-btn:hover {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
  }

  .art-top-btn:active,
  .art-btn:active {
    scale: 0.95;
  }

  /* 点赞按钮：尺寸与「回复」按钮一致，主题色为粉红色爱心 */
  .art-like-btn {
    display: inline-flex;
    align-items: center;
    gap: 3px;
    height: 26px;
    padding: 0 10px;
    margin-top: 2px;
    border: 1px solid rgba(230, 120, 130, 0.36);
    border-radius: 8px;
    background: transparent;
    color: #e65c6c;
    font-size: 0.75em;
    font-weight: 500;
    line-height: 1;
    cursor: pointer;
    user-select: none;
    transition: border-color 0.2s ease, background 0.2s ease, color 0.2s ease, scale 0.1s ease;
  }

  .art-like-btn:hover {
    border-color: rgba(230, 120, 130, 0.6);
    background: rgba(230, 120, 130, 0.08);
  }

  .art-like-btn:active {
    scale: 0.93;
  }

  .art-like-btn[aria-pressed="true"] {
    border-color: #e65c6c;
    background: rgba(230, 92, 108, 0.12);
    color: #ff4d5e;
  }

  .art-like-btn svg {
    flex-shrink: 0;
  }

  .art-fold-btn {
    display: block;
    width: fit-content;
    margin: 0.5em auto 0;
    padding: 0;
    border: 0;
    background: transparent;
    color: #999;
    font-size: 0.75em;
    line-height: 1.5;
    cursor: pointer;
    user-select: none;
    transition: color 0.2s ease;
  }

  .art-fold-btn:hover {
    color: #27ae60;
  }

  .art-list-fold {
    margin: 0.75em auto 0;
  }

  .art-card-body {
    margin: 0.625em 0 0.75em;
    color: #334155;
    font-size: 0.875rem;
    line-height: 2;
    word-break: break-word;
  }

  .art-card-body img {
    max-width: 100%;
  }

  /* 楼中楼：参考站同款左侧虚线引用块 */
  .art-child-list {
    margin-top: 0.5em;
    border-left: 1px dashed rgba(0, 0, 0, 0.09);
  }

  .art-child {
    display: flex;
    padding: 0.5em 0 0.5em 0.5em;
    margin: 0;
    border: 0;
    border-radius: 0;
    background: transparent;
  }

  .art-child .art-card {
    padding-bottom: 0;
    border-bottom: 0;
  }

  .art-child .art-meta {
    margin-top: 0.25em;
  }

  .art-child-avatar {
    width: 2.25rem;
    height: 2.25rem;
    font-size: 12px;
    line-height: 2.25rem;
  }

  .art-child-body {
    margin-top: 0.5em;
    font-size: 0.8125rem;
    line-height: 1.9;
  }

  .art-child-at {
    margin-right: 4px;
    color: #0f766e;
    font-weight: 600;
  }

  /* 行内回复条：描边框只包输入框本体（与留言页一致），发送/取消落在框外右侧 */
  .art-inline-reply {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 12px 0 2px 46px;
    padding: 0;
    background: transparent;
  }

  .art-inline-input {
    display: block;
    box-sizing: border-box;
    width: 100%;
    height: 30px;
    padding: 0 10px;
    border: none;
    border-radius: 0;
    outline: none;
    background: transparent;
    color: #4b5563;
    font-size: 13px;
  }

  /* 输入框自己描边：宽度 = 行宽 - 发送 - 取消 - 两侧 8px 间距，右端不再顶到发送 */
  .art-inline-reply :deep(.float-field) {
    flex: 1 1 auto;
    min-width: 0;
    --float-radius: 10px;
    --float-notch-left: 12px;
    --float-label-size: 13px;
    --float-border-color: rgba(120, 120, 120, 0.16);
    --float-label-color: #9ca3af;
    --float-label-active-color: #4b5563;
    --float-accent-color: var(--themeBackground);
    --float-margin-bottom: 0;
  }

  .art-inline-reply :deep(.float-field:focus-within .float-border) {
    border-color: color-mix(in srgb, var(--themeBackground) 50%, transparent);
  }

  .art-inline-send,
  .art-inline-cancel {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    padding: 6px 12px;
    border: 1px solid rgba(120, 120, 120, 0.16);
    border-radius: 10px;
    background: transparent;
    color: var(--themeBackground);
    font-size: 14px;
    font-weight: 500;
    line-height: 20px;
    cursor: pointer;
    user-select: none;
    box-shadow:
      0 2px 5px rgba(38, 38, 38, 0.07),
      0 1px 2px rgba(38, 38, 38, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 0.8),
      inset 0 -1px 0 rgba(112, 112, 112, 0.1);
    transition: color 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background-color 0.22s ease;
  }

  .art-inline-send:hover,
  .art-inline-cancel:hover {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
  }

  .art-inline-send:active,
  .art-inline-cancel:active {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
    scale: 0.95;
  }

  @media (prefers-reduced-motion: reduce) {
    .art-btn,
    .art-fold-btn,
    .art-inline-reply,
    .art-inline-send,
    .art-inline-cancel {
      transition: none;
    }

    .art-btn:active,
    .art-fold-btn:active {
      scale: 1;
    }
  }

  html.dark .art-list-title-main {
    color: #5eead4;
  }

  html.dark .art-sort li {
    border-color: rgba(255, 255, 255, 0.16);
    color: #94a3b8;
  }

  html.dark .art-sort li:hover {
    border-color: rgba(255, 255, 255, 0.3);
    color: #cbd5e1;
  }

  html.dark .art-sort li.active {
    border-color: #5eead4;
    background: #5eead4;
    color: #0f172a;
  }

  html.dark .art-list-title-divider {
    background: #334155;
  }

  html.dark .art-list-title-sub {
    color: #94a3b8;
  }

  html.dark .art-card {
    border-bottom-color: rgba(255, 255, 255, 0.08);
  }

  html.dark .art-top-tag {
    background: #dc2626;
  }

  html.dark .art-loc,
  html.dark .art-browser,
  html.dark .art-os {
    border-color: rgba(255, 255, 255, 0.1);
    background: rgba(255, 255, 255, 0.06);
    color: #94a3b8;
  }

  html.dark .art-date {
    color: #94a3b8;
  }

  html.dark .art-reply-count {
    color: #94a3b8;
  }

  html.dark .art-top-btn,
  html.dark .art-btn {
    border-color: rgba(255, 255, 255, 0.16);
    color: var(--themeBackground);
    box-shadow:
      0 2px 5px rgba(0, 0, 0, 0.3),
      0 1px 2px rgba(0, 0, 0, 0.24),
      inset 0 1px 0 rgba(255, 255, 255, 0.06),
      inset 0 -1px 0 rgba(0, 0, 0, 0.3);
  }

  html.dark .art-top-btn:hover,
  html.dark .art-btn:hover,
  html.dark .art-top-btn:active,
  html.dark .art-btn:active {
    border-color: rgba(255, 255, 255, 0.4);
    color: var(--themeBackground);
    box-shadow:
      0 3px 6px rgba(0, 0, 0, 0.38),
      0 1px 2px rgba(0, 0, 0, 0.28),
      inset 0 1px 0 rgba(255, 255, 255, 0.1),
      inset 0 -1px 0 rgba(0, 0, 0, 0.34);
  }

  html.dark .art-fold-btn {
    color: #94a3b8;
  }

  html.dark .art-fold-btn:hover {
    color: #2ecc71;
  }

  html.dark .art-like-btn {
    border-color: rgba(255, 140, 150, 0.3);
    color: #ff9aa7;
  }

  html.dark .art-like-btn:hover {
    border-color: rgba(255, 140, 150, 0.55);
    background: rgba(255, 120, 130, 0.12);
  }

  html.dark .art-like-btn[aria-pressed="true"] {
    border-color: #ff6b7a;
    background: rgba(255, 107, 122, 0.18);
    color: #ff6b7a;
  }

  html.dark .art-card-body {
    color: #cbd5e1;
  }

  html.dark .art-child-list {
    border-left-color: rgba(255, 255, 255, 0.1);
  }

  html.dark .art-child {
    border-color: transparent;
    background: transparent;
  }

  html.dark .art-child-at {
    color: #5eead4;
  }

  html.dark .art-inline-input {
    color: #e2e8f0;
  }

  html.dark .art-inline-reply :deep(.float-field) {
    --float-border-color: rgba(255, 255, 255, 0.16);
    --float-label-color: #94a3b8;
    --float-label-active-color: #e2e8f0;
    --float-accent-color: var(--themeBackground);
  }

  html.dark .art-inline-reply :deep(.float-field:focus-within .float-border) {
    border-color: rgba(99, 179, 255, 0.5);
  }

  html.dark .art-inline-send,
  html.dark .art-inline-cancel {
    border-color: rgba(255, 255, 255, 0.16);
    color: var(--themeBackground);
    box-shadow:
      0 2px 5px rgba(0, 0, 0, 0.3),
      0 1px 2px rgba(0, 0, 0, 0.24),
      inset 0 1px 0 rgba(255, 255, 255, 0.06),
      inset 0 -1px 0 rgba(0, 0, 0, 0.3);
  }

  html.dark .art-inline-send:hover,
  html.dark .art-inline-cancel:hover,
  html.dark .art-inline-send:active,
  html.dark .art-inline-cancel:active {
    border-color: rgba(255, 255, 255, 0.4);
    color: var(--themeBackground);
    box-shadow:
      0 3px 6px rgba(0, 0, 0, 0.38),
      0 1px 2px rgba(0, 0, 0, 0.28),
      inset 0 1px 0 rgba(255, 255, 255, 0.1),
      inset 0 -1px 0 rgba(0, 0, 0, 0.34);
  }

  @media screen and (max-width: 600px) {
    .art-avatar {
      width: 2.25rem;
      height: 2.25rem;
      font-size: 12px;
      line-height: 2.25rem;
    }

    .art-child-avatar {
      width: 2rem;
      height: 2rem;
      line-height: 2rem;
    }

    .art-inline-reply {
      flex-wrap: wrap;
      gap: 8px;
    }

    .art-inline-reply :deep(.float-field) {
      flex: 1 1 100%;
    }

    .art-inline-send {
      margin-left: auto;
    }

    /* 窄屏：评论段左右内边距收窄，标题分隔线用同位负外边距继续通栏 */
    .art-comment {
      padding: 14px 14px 16px;
    }

    .art-comment .art-list-title {
      margin-left: -14px;
      margin-right: -14px;
      padding: 0 14px 10px;
    }

    .art-comment-card > .myCenter {
      padding: 16px 14px;
    }
  }
</style>

<!-- 表情面板通过 el-popover 传送到 body，需用非 scoped 样式贴合评论区配色 -->
<style>
  /* 面板宽度由 el-popover 的 :width 控制（宽度是内联样式，CSS 里的 width 会被覆盖）；
     这里只兜住窄屏上限，格子用弹性列，宽度被压缩时也不会溢出底板 */
  .art-comment-emoji-popper.el-popper {
    box-sizing: border-box;
    max-width: calc(100vw - 24px);
    padding: 10px;
    border: 1px solid #d8f1eb;
    border-radius: 14px;
    background: #f0fdfb;
    box-shadow: 0 12px 30px rgba(17, 48, 44, 0.14);
  }

  html.dark .art-comment-emoji-popper.el-popper {
    background: #11302c;
    border-color: rgba(255, 255, 255, 0.10);
    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.45);
  }

  .art-comment-emoji-popper .el-popper__arrow {
    display: none;
  }

  /* 评论正文里由 token 渲染出的图片表情（v-html 内容，scoped 样式覆盖不到） */
  .art-comment-emoji-img {
    display: inline-block;
    width: 24px;
    height: 24px;
    margin: 0 1px;
    vertical-align: middle;
  }
</style>
