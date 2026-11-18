import axios from "axios";
import constant from "./constant";
//处理url参数
import qs from "qs";

import store from "../store";
import { clearAdminEntry } from "./adminEntry";


axios.defaults.baseURL = constant.baseURL;


// 添加请求拦截器
axios.interceptors.request.use(function (config) {
  // 在发送请求之前做些什么
  return config;
}, function (error) {
  // 对请求错误做些什么
  return Promise.reject(error);
});

// 添加响应拦截器
axios.interceptors.response.use(function (response) {
  if (response.data !== null && response.data.hasOwnProperty("code") && response.data.code !== 200) {
    if (response.data.code === 300) {
      // 只有请求确实携带过 token 才算登录态失效；匿名访客请求到需登录的接口时，
      // 不能清空登录信息并整站跳转，否则一个访客接口失败就会误伤所有人
      const authorization = response.config && response.config.headers
        ? response.config.headers.Authorization
        : null;
      if (authorization) {
        store.commit("loadCurrentUser", {});
        localStorage.removeItem("userToken");
        store.commit("loadCurrentAdmin", {});
        localStorage.removeItem("adminToken");
        clearAdminEntry();
        window.location.href = constant.webURL + "/user";
      }
    }
    return Promise.reject(new Error(response.data.message));
  } else {
    return response;
  }
}, function (error) {
  // 对响应错误做点什么：把 axios 默认英文错误信息翻译成中文，方便用户理解
  if (error && error.message) {
    if (error.message === "Network Error") {
      error.message = "网络连接失败，请检查网络或稍后重试";
    } else if (error.code === "ECONNABORTED" || error.message.indexOf("timeout") !== -1) {
      error.message = "请求超时，请稍后重试";
    }
  }
  return Promise.reject(error);
});

// 当data为URLSearchParams对象时设置为application/x-www-form-urlencoded;charset=utf-8
// 当data为普通对象时，会被设置为application/json;charset=utf-8


export default {
  post(url, params = {}, isAdmin = false, json = true) {
    let config;
    if (isAdmin) {
      config = {
        headers: {"Authorization": localStorage.getItem("adminToken")}
      };
    } else {
      config = {
        headers: {"Authorization": localStorage.getItem("userToken")}
      };
    }

    return new Promise((resolve, reject) => {
      axios
        .post(url, json ? params : qs.stringify(params), config)
        .then(res => {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  },

  get(url, params = {}, isAdmin = false) {
    let headers;
    if (isAdmin) {
      headers = {"Authorization": localStorage.getItem("adminToken")};
    } else {
      headers = {"Authorization": localStorage.getItem("userToken")};
    }

    return new Promise((resolve, reject) => {
      axios.get(url, {
        params: params,
        headers: headers
      }).then(res => {
        resolve(res.data);
      }).catch(err => {
        reject(err)
      })
    });
  },

  upload(url, param, isAdmin = false, option) {
    let config;
    if (isAdmin) {
      config = {
        headers: {"Authorization": localStorage.getItem("adminToken"), "Content-Type": "multipart/form-data"},
        timeout: 60000
      };
    } else {
      config = {
        headers: {"Authorization": localStorage.getItem("userToken"), "Content-Type": "multipart/form-data"},
        timeout: 60000
      };
    }
    if (typeof option !== "undefined") {
      config.onUploadProgress = progressEvent => {
        if (progressEvent.total > 0) {
          progressEvent.percent = progressEvent.loaded / progressEvent.total * 100;
        }
        option.onProgress(progressEvent);
      };
    }

    return new Promise((resolve, reject) => {
      axios
        .post(url, param, config)
        .then(res => {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  },

  uploadQiniu(url, param) {
    let config = {
      headers: {"Content-Type": "multipart/form-data"},
      timeout: 60000
    };

    return new Promise((resolve, reject) => {
      axios
        .post(url, param, config)
        .then(res => {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  }
}
