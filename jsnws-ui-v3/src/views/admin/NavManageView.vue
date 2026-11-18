<template>
  <div class="admin-card">
    <div class="admin-page-head">
      <h2>导航管理</h2>
      <p>控制前台导航项的显示与顺序，登录按钮与后台入口按权限规则固定展示，不在此处管理。</p>
    </div>

    <div class="nav-tip">
      排序靠前的导航项优先显示；关闭显示后对应入口不再出现在导航中，站内页面仍可通过链接访问。
    </div>

    <ul class="nav-list">
      <li v-for="(item, index) in navList"
          :key="item.key"
          class="nav-row"
          :class="{ 'is-hidden': !item.visible }">
        <div class="nav-info">
          <span class="nav-title">{{item.title}}</span>
          <span class="nav-desc">{{item.desc}}</span>
        </div>
        <div class="nav-ops">
          <span class="nav-order">第 {{index + 1}} 位</span>
          <el-switch v-model="item.visible"
                     active-text="显示"
                     inactive-text="隐藏"></el-switch>
          <el-button size="small" :disabled="index === 0" @click="move(index, -1)">上移</el-button>
          <el-button size="small" :disabled="index === navList.length - 1" @click="move(index, 1)">下移</el-button>
        </div>
      </li>
    </ul>

    <div class="nav-foot">
      <el-button type="primary" :loading="saving" @click="save()">保存导航配置</el-button>
    </div>
  </div>
</template>

<script>
  // 前台导航项元数据：key 与 HomeLayout 中的渲染分支一一对应
  const NAV_META = [
    {key: "home", title: "主页", desc: "点击进入首页 /"},
    {key: "article", title: "文章", desc: "下拉：文章列表 / 图谱 / 归档"},
    {key: "anime", title: "追番", desc: "点击进入追番页 /anime"},
    {key: "dynamic", title: "动态", desc: "下拉：随笔 / 相册 / 留言"},
    {key: "about", title: "关于", desc: "下拉：关于我 / 友链 / 打赏"}
  ]

  export default {
    name: "NavManageView",
    data() {
      return {
        navList: NAV_META.map((item) => {
          return {
            key: item.key,
            title: item.title,
            desc: item.desc,
            visible: true
          }
        }),
        configId: null,
        saving: false
      }
    },
    created() {
      this.getNavConfig();
    },
    methods: {
      getNavConfig() {
        this.$http.get(this.$constant.baseURL + "/sysConfig/listConfig", {}, true)
          .then((res) => {
            const list = this.$common.isEmpty(res.data) ? [] : res.data;
            const target = list.find((item) => item.configKey === "navConfig");
            let saved = [];
            if (target && target.configValue) {
              this.configId = target.id;
              try {
                const parsed = JSON.parse(target.configValue);
                saved = Array.isArray(parsed) ? parsed : [];
              } catch (e) {
                saved = [];
              }
            }
            const map = {};
            saved.forEach((item, index) => {
              if (item && item.key) {
                map[item.key] = {visible: item.visible !== false, order: index};
              }
            });
            this.navList = NAV_META.map((item, index) => {
              const config = map[item.key];
              return {
                key: item.key,
                title: item.title,
                desc: item.desc,
                visible: config ? config.visible : true,
                order: config ? config.order : NAV_META.length + index
              }
            }).sort((a, b) => a.order - b.order);
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      move(index, step) {
        const target = index + step;
        if (target < 0 || target >= this.navList.length) {
          return;
        }
        const list = this.navList.slice();
        const temp = list[index];
        list[index] = list[target];
        list[target] = temp;
        this.navList = list;
      },
      save() {
        const value = this.navList.map((item) => {
          return {
            key: item.key,
            visible: item.visible
          }
        });
        this.saving = true;
        this.$http.post(this.$constant.baseURL + "/sysConfig/saveOrUpdateConfig", {
          id: this.configId,
          configName: "前台导航配置",
          configKey: "navConfig",
          configValue: JSON.stringify(value),
          // 2 = 公开配置，前台通过 /sysConfig/listSysConfig 读取
          configType: "2"
        }, true).then(() => {
          this.saving = false;
          this.getNavConfig();
          this.refreshStoreSysConfig();
          this.$message({
            message: "保存成功！",
            type: "success"
          });
        }).catch((error) => {
          this.saving = false;
          this.$message({
            message: error.message,
            type: "error"
          });
        });
      },
      refreshStoreSysConfig() {
        this.$http.get(this.$constant.baseURL + "/sysConfig/listSysConfig")
          .then((res) => {
            this.$store.commit("loadSysConfig", this.$common.isEmpty(res.data) ? {} : res.data);
          })
          .catch(() => {
          });
      }
    }
  }
</script>

<style scoped>
  .nav-tip {
    margin-bottom: 16px;
    padding: 10px 14px;
    border-radius: 10px;
    background: var(--admin-subtle);
    font-size: 12px;
    line-height: 1.7;
    color: var(--admin-text-secondary);
  }

  .nav-list {
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .nav-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 14px 16px;
    margin-bottom: 10px;
    border: 1px solid var(--admin-border);
    border-radius: 10px;
    transition: border-color 0.2s ease, opacity 0.2s ease;
  }

  .nav-row.is-hidden {
    opacity: 0.6;
  }

  .nav-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-width: 0;
  }

  .nav-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--admin-text);
  }

  .nav-desc {
    font-size: 12px;
    color: var(--admin-text-secondary);
  }

  .nav-ops {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-shrink: 0;
  }

  .nav-order {
    font-size: 12px;
    color: var(--admin-text-secondary);
  }

  .nav-foot {
    display: flex;
    justify-content: flex-end;
    padding-top: 8px;
  }
</style>
