/**
 * 评论 / 留言共用的 Waline 图片表情配置与渲染逻辑
 * 三套包（weibo / bilibili / bmoji）与 @waline/emojis@1.4.0 的目录、文件名完全一致。
 * cdn.jsdelivr.net 在部分网络下 TLS 握手失败，这里统一走 jsdelivr 官方镜像 fastly，
 * 需要换回主域时只改下面这一处地址即可，两个页面同步生效。
 */
export const EMOJI_BASE_URL = "https://fastly.jsdelivr.net/npm/@waline/emojis@1.4.0/";

export const EMOJI_GROUPS = [
  {
    key: "weibo",
    name: "微博",
    // 文件名用空格拼成一条，避免上百个对象占内存；使用时按空格拆开
    names: "weibo_alpaca weibo_angry weibo_annoyed weibo_annoyed_left weibo_annoyed_right " +
      "weibo_antic weibo_awkward weibo_blush weibo_boy weibo_bye weibo_cat weibo_cat_annoyed " +
      "weibo_cat_bye weibo_cat_cry weibo_cat_think weibo_chuckle weibo_clap weibo_confused " +
      "weibo_desire weibo_disappointed weibo_dizzy_face weibo_dog_annoyed weibo_dog_bye " +
      "weibo_dog_consider weibo_dog_cry weibo_dog_joy weibo_dog_laugh weibo_dog_sweat " +
      "weibo_dog_think weibo_dog_yum weibo_doge weibo_flushed weibo_geili weibo_girl " +
      "weibo_girl_annoyed weibo_greddy weibo_grievance weibo_grin weibo_hammer weibo_heart_eyes " +
      "weibo_hufen weibo_husky weibo_jiong weibo_joy weibo_kiss weibo_laugh weibo_longing " +
      "weibo_look_down weibo_love weibo_lovely weibo_mask weibo_meng weibo_money weibo_no " +
      "weibo_no_idea weibo_panda weibo_pick_nose weibo_pig weibo_poor weibo_quiet weibo_rabbit " +
      "weibo_rage weibo_rolling_eyes weibo_sad weibo_scream weibo_shenma weibo_shuai weibo_shy " +
      "weibo_sick weibo_slap weibo_sleep weibo_sleepy weibo_slient weibo_smile weibo_smirk " +
      "weibo_soap weibo_sob weibo_sunglasses weibo_suprised weibo_think weibo_ultraman " +
      "weibo_vomit weibo_watermalon weibo_wink weibo_wool_group weibo_xi weibo_yan weibo_yawn " +
      "weibo_yum"
  },
  {
    key: "bilibili",
    name: "B站",
    names: "bb_annoyed bb_antic bb_awkward bb_bye bb_chuckle bb_clap bb_confused bb_crazy " +
      "bb_cry bb_cute bb_dizzy_face bb_doge bb_face_cry bb_flushed bb_greddy bb_grievance " +
      "bb_heart_eyes bb_kiss bb_look_down bb_lovely bb_mask bb_miantian bb_money bb_no_mouth " +
      "bb_nosebleed bb_not_care bb_pick_nose bb_question_mask bb_rage bb_rolling_eyes bb_sad " +
      "bb_scared bb_shy bb_slap bb_sleep bb_sleepy bb_slient bb_smirk bb_sob bb_spit_blodd " +
      "bb_sunglasses bb_sweat bb_test bb_think bb_thumbsup bb_tiaokan bb_trollface bb_vomit " +
      "bb_yum bb_yum1 bb_zhoumei"
  },
  {
    key: "bmoji",
    name: "bmoji",
    names: "bmoji_aaa bmoji_abandon bmoji_ah bmoji_angry bmoji_applaud bmoji_bao_fu " +
      "bmoji_blush bmoji_blushing bmoji_bravo bmoji_bye bmoji_call bmoji_cheers bmoji_cold " +
      "bmoji_comical bmoji_cool bmoji_crack_melon_seeds bmoji_cute bmoji_doge bmoji_drumstick " +
      "bmoji_dudu bmoji_emm bmoji_fu bmoji_give_love bmoji_good bmoji_grimace bmoji_grin " +
      "bmoji_haha bmoji_hard bmoji_heartbeat bmoji_hehe bmoji_hei bmoji_heihei bmoji_hem " +
      "bmoji_hmm bmoji_hot bmoji_hug_together bmoji_hurt bmoji_ill bmoji_insidious bmoji_jiong " +
      "bmoji_knees bmoji_koi bmoji_lemon bmoji_love bmoji_mask bmoji_my_fault bmoji_naughty " +
      "bmoji_not_open bmoji_oh bmoji_ok bmoji_onlooker bmoji_oops bmoji_palming bmoji_please " +
      "bmoji_roll_eye bmoji_sad bmoji_scared bmoji_shh bmoji_shui_dao bmoji_silme " +
      "bmoji_sleeply bmoji_smile_cry bmoji_smirking bmoji_snow bmoji_spit bmoji_stagnate " +
      "bmoji_strong bmoji_thanks bmoji_thumb_up bmoji_tibetan_fox bmoji_titter bmoji_um " +
      "bmoji_um_2 bmoji_unavailble_doge bmoji_unhappy bmoji_wa bmoji_wail bmoji_what " +
      "bmoji_win bmoji_wow"
  }
];

// 包名 → 该包内真实存在的文件名集合，渲染时用来拦掉手输的无效 token
const EMOJI_NAME_INDEX = EMOJI_GROUPS.reduce((index, group) => {
  index[group.key] = new Set(group.names.split(" "));
  return index;
}, {});

const EMOJI_TOKEN_REG = /:([a-z]+)\/([A-Za-z0-9_]+):/g;

export function emojiNames(group) {
  return group ? group.names.split(" ") : [];
}

export function emojiUrl(pack, name) {
  return EMOJI_BASE_URL + pack + "/" + name + ".png";
}

// 正文 / 评论共用的一张表情图片（24px，与评论输入框尺寸一致）
export function emojiImageHtml(pack, name) {
  const token = emojiToken(pack, name);
  return '<img class="art-comment-emoji-img" loading="lazy" width="24" height="24" title="'
    + token + '" alt="' + token + '" src="' + emojiUrl(pack, name) + '"/>';
}

/**
 * markdown-it 插件：把 :包名/文件名: 识别成行内图片。
 * 只在正文（html:true 的渲染管线）里用，代码块 / 代码段里的 token 不会被误替换。
 */
export function emojiPlugin(md) {
  md.inline.ruler.before("emphasis", "jsnws_emoji", (state, silent) => {
    const start = state.pos;
    if (state.src.charCodeAt(start) !== 0x3A) {
      return false;
    }
    const match = /^:([a-z]+)\/([A-Za-z0-9_]+):/.exec(state.src.slice(start, start + 64));
    if (!match) {
      return false;
    }
    const names = EMOJI_NAME_INDEX[match[1]];
    if (!names || !names.has(match[2])) {
      return false;
    }
    if (!silent) {
      const token = state.push("html_inline", "", 0);
      token.content = emojiImageHtml(match[1], match[2]);
    }
    state.pos = start + match[0].length;
    return true;
  });
}

// 插入正文用的 token，采用 Waline 的 :包名/文件名: 写法
export function emojiToken(pack, name) {
  return ":" + pack + "/" + name + ":";
}

/**
 * 把正文里的表情 token 换成 24px 图片（评论与留言列表共用）。
 * 后端只存原文、不解析表情，所以渲染放在前端；只认三套内置包名，
 * 且文件名必须在该包清单里，乱写的 token 原样保留为文字。
 */
export function renderEmojiImages(content) {
  if (!content || content.indexOf(":") === -1) {
    return content;
  }
  return content.replace(EMOJI_TOKEN_REG, (token, pack, name) => {
    const names = EMOJI_NAME_INDEX[pack];
    if (!names || !names.has(name)) {
      return token;
    }
    return emojiImageHtml(pack, name);
  });
}
