// B 站分类（season_type）的唯一映射来源，前台追番页与后台追番管理共用
export const SEASON_TYPES = {
  1: "番剧",
  2: "电影",
  3: "纪录片",
  4: "国创",
  5: "电视剧",
  7: "综艺"
};

// 下拉选项：顺序与数值升序一致
export const SEASON_TYPE_OPTIONS = Object.keys(SEASON_TYPES)
  .map(Number)
  .sort((a, b) => a - b)
  .map((value) => {
    return {
      value: value,
      label: SEASON_TYPES[value]
    };
  });

// 未知或空值返回「—」，调用方需要默认番剧时自行传 1
export function seasonTypeLabel(seasonType) {
  return SEASON_TYPES[seasonType] || "—";
}
