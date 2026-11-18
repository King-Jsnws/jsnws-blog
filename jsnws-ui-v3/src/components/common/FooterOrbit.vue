<template>
  <!-- 页脚运行时长徽章里的地球：参考站 footer-orbit 同款（地球自转 + 月球环绕），尺寸沿用原图标槽位 -->
  <span class="footer-orbit" aria-hidden="true">
    <img v-show="!ready && !failed"
         class="footer-orbit-poster"
         src="/images/footer-orbit/poster.webp"
         width="144" height="144" alt="">
    <canvas v-show="!failed" ref="canvas" width="144" height="144"></canvas>
    <svg v-if="failed" class="footer-orbit-fallback" viewBox="0 0 24 24" aria-hidden="true">
      <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" stroke-width="1.6"/>
      <ellipse cx="12" cy="12" rx="4" ry="9" fill="none" stroke="currentColor" stroke-width="1.4"/>
      <path d="M3.2 9h17.6M3.2 15h17.6" fill="none" stroke="currentColor" stroke-width="1.4"/>
    </svg>
  </span>
</template>

<script>
  const EARTH_SRC = "/images/footer-orbit/earth.webp";
  const MOON_SRC = "/images/footer-orbit/moon.webp";

  // 把等距圆柱投影的贴图映射成球面像素缓存：UV + 光照一次算好，逐帧只做采样
  function buildSphere(texture, size, tilt = 0) {
    const pixels = new Uint8ClampedArray(size * size * 4);
    const radius = size / 2;
    const cos = Math.cos(tilt);
    const sin = Math.sin(tilt);
    const points = [];
    for (let row = 0; row < size; row++) {
      for (let col = 0; col < size; col++) {
        const x = (col + 0.5 - radius) / radius;
        const y = (radius - row - 0.5) / radius;
        const distance = Math.hypot(x, y);
        if (distance >= 1) {
          continue;
        }
        const z = Math.sqrt(1 - distance * distance);
        const h = x * cos + y * sin;
        const s = -x * sin + y * cos;
        const light = Math.max(0, -0.38 * x + 0.46 * y + 0.8 * z);
        const index = (row * size + col) * 4;
        pixels[index + 3] = Math.min(1, (1 - distance) * radius) * 255;
        points.push({
          index,
          u: Math.atan2(h, z) / (Math.PI * 2) + 0.5,
          v: Math.acos(s) / Math.PI * (texture.height - 1),
          light: Math.sqrt(0.22 + 0.78 * light)
        });
      }
    }
    return {
      draw(offset) {
        for (const point of points) {
          const u = ((point.u + offset) % 1 + 1) % 1 * texture.width;
          const col = Math.floor(u);
          const row = Math.floor(point.v);
          const fx = u - col;
          const fy = point.v - row;
          const nextCol = (col + 1) % texture.width;
          const nextRow = Math.min(row + 1, texture.height - 1);
          const i00 = (row * texture.width + col) * 4;
          const i10 = (row * texture.width + nextCol) * 4;
          const i01 = (nextRow * texture.width + col) * 4;
          const i11 = (nextRow * texture.width + nextCol) * 4;
          for (let channel = 0; channel < 3; channel++) {
            const top = texture.data[i00 + channel] * (1 - fx) + texture.data[i10 + channel] * fx;
            const bottom = texture.data[i01 + channel] * (1 - fx) + texture.data[i11 + channel] * fx;
            pixels[point.index + channel] = (top * (1 - fy) + bottom * fy) * point.light;
          }
        }
        return pixels;
      }
    };
  }

  function loadTexture(src) {
    return new Promise((resolve, reject) => {
      const image = new Image();
      image.onload = () => {
        const canvas = document.createElement("canvas");
        canvas.width = image.naturalWidth;
        canvas.height = image.naturalHeight;
        const ctx = canvas.getContext("2d", {willReadFrequently: true});
        if (!ctx) {
          reject(new Error("canvas unavailable"));
          return;
        }
        ctx.drawImage(image, 0, 0);
        resolve({
          data: ctx.getImageData(0, 0, canvas.width, canvas.height).data,
          width: canvas.width,
          height: canvas.height
        });
      };
      image.onerror = () => reject(new Error("texture load failed"));
      image.src = src;
    });
  }

  export default {
    name: "FooterOrbit",
    data() {
      return {
        failed: false,
        ready: false
      };
    },
    mounted() {
      this.motion = window.matchMedia("(prefers-reduced-motion: reduce)");
      this.frame = 0;
      this.lastFrame = 0;
      this.elapsed = 0;
      this.inView = false;
      this.loading = false;
      this.drawScene = null;
      this.destroyed = false;
      this.motion.addEventListener("change", this.updatePlayback);
      document.addEventListener("visibilitychange", this.updatePlayback);
      this.observer = new IntersectionObserver((entries) => {
        this.inView = entries[0].isIntersecting;
        this.updatePlayback();
      });
      this.observer.observe(this.$el);
    },
    beforeUnmount() {
      this.destroyed = true;
      this.pause();
      if (this.observer) {
        this.observer.disconnect();
      }
      this.motion.removeEventListener("change", this.updatePlayback);
      document.removeEventListener("visibilitychange", this.updatePlayback);
    },
    methods: {
      async prepare() {
        this.loading = true;
        try {
          const [earth, moon] = await Promise.all([loadTexture(EARTH_SRC), loadTexture(MOON_SRC)]);
          // 卸载后贴图才加载完时直接放弃，避免操作已销毁的 canvas
          if (this.destroyed || !this.$refs.canvas) {
            return;
          }
          const canvas = this.$refs.canvas;
          const ctx = canvas.getContext("2d");
          if (!ctx) {
            return;
          }
          // 绘制坐标系固定 72×72；缓冲按设备像素比放大，DPR≥2 时与参考站一样是 144×144
          const dpr = Math.max(2, Math.min(window.devicePixelRatio || 1, 3));
          canvas.width = Math.round(72 * dpr);
          canvas.height = Math.round(72 * dpr);
          ctx.scale(dpr, dpr);
          const earthSphere = buildSphere(earth, 88, 23.4 * Math.PI / 180);
          const moonSphere = buildSphere(moon, 24);
          const earthCanvas = document.createElement("canvas");
          const moonCanvas = document.createElement("canvas");
          earthCanvas.width = earthCanvas.height = 88;
          moonCanvas.width = moonCanvas.height = 24;
          const earthCtx = earthCanvas.getContext("2d");
          const moonCtx = moonCanvas.getContext("2d");
          if (!earthCtx || !moonCtx) {
            return;
          }
          const earthImage = new ImageData(88, 88);
          const moonImage = new ImageData(24, 24);
          const paint = (sphere, image, target, offset) => {
            image.data.set(sphere.draw(offset));
            target.putImageData(image, 0, 0);
          };
          const moonPosition = (angle) => {
            const tilt = -24 * Math.PI / 180;
            const x = 29.5 * Math.cos(angle);
            const z = 10.9 * Math.sin(angle);
            const depth = 27.4 * Math.sin(angle);
            const scale = 200 / (200 - depth);
            return {
              x: 36 + (x * Math.cos(tilt) - z * Math.sin(tilt)) * scale,
              y: 36 + (x * Math.sin(tilt) + z * Math.cos(tilt)) * scale,
              depth,
              scale
            };
          };

          this.drawScene = (time) => {
            const angle = time / 8 * Math.PI * 2 + 0.8;
            const moonPos = moonPosition(angle);
            const moonSize = 6 * moonPos.scale;
            paint(earthSphere, earthImage, earthCtx, time / 32 + 0.08);
            paint(moonSphere, moonImage, moonCtx, angle / (Math.PI * 2));
            ctx.clearRect(0, 0, 72, 72);
            // 每帧只读一次当前文字色，供两条轨道线复用（跟随主题切换）
            const orbitColor = getComputedStyle(this.$el).color;

            const strokeOrbit = (front) => {
              ctx.save();
              ctx.strokeStyle = orbitColor;
              ctx.globalAlpha = front ? 0.65 : 0.28;
              ctx.lineWidth = 0.7;
              ctx.beginPath();
              for (let step = 0; step <= 64; step++) {
                const point = moonPosition((front ? 0 : Math.PI) + step / 64 * Math.PI);
                if (step === 0) {
                  ctx.moveTo(point.x, point.y);
                } else {
                  ctx.lineTo(point.x, point.y);
                }
              }
              ctx.stroke();
              ctx.restore();
            };
            const drawMoon = () => ctx.drawImage(moonCanvas, moonPos.x - moonSize, moonPos.y - moonSize, moonSize * 2, moonSize * 2);

            strokeOrbit(false);
            if (moonPos.depth < 0) {
              drawMoon();
            }
            ctx.drawImage(earthCanvas, 14, 14, 44, 44);
            ctx.beginPath();
            ctx.arc(36, 36, 22, 0, Math.PI * 2);
            ctx.strokeStyle = "rgba(104, 183, 242, 0.6)";
            ctx.lineWidth = 0.5;
            ctx.stroke();
            strokeOrbit(true);
            if (moonPos.depth >= 0) {
              drawMoon();
            }
          };

          this.drawScene(this.elapsed);
          this.ready = true;
          this.updatePlayback();
        } catch (e) {
          this.failed = true;
        } finally {
          this.loading = false;
        }
      },
      pause() {
        cancelAnimationFrame(this.frame);
        this.frame = 0;
        this.lastFrame = 0;
      },
      updatePlayback() {
        if (this.destroyed) {
          return;
        }
        if (this.motion.matches || !this.inView || document.hidden) {
          this.pause();
          return;
        }
        if (!this.drawScene) {
          if (!this.loading) {
            this.prepare();
          }
          return;
        }
        if (!this.frame) {
          this.frame = requestAnimationFrame(this.tick);
        }
      },
      tick(time) {
        if (this.destroyed) {
          return;
        }
        if (!this.lastFrame) {
          this.lastFrame = time;
        }
        const delta = time - this.lastFrame;
        if (delta >= 1000 / 30) {
          this.elapsed += Math.min(delta, 100) / 1000;
          this.lastFrame = time;
          if (this.drawScene) {
            this.drawScene(this.elapsed);
          }
        }
        this.frame = requestAnimationFrame(this.tick);
      }
    }
  };
</script>

<style scoped>
  .footer-orbit {
    /* 绝对定位在徽章左侧，64×64 不撑高徽章，与参考站页脚地球的悬浮方式一致 */
    position: absolute;
    top: 50%;
    left: 20px;
    display: inline-flex;
    flex-shrink: 0;
    /* 参考站该 canvas 的 CSS 实际显示尺寸就是 64×64（144×144 是 2 倍像素缓冲） */
    width: 64px;
    height: 64px;
    transform: translateY(-50%);
    color: var(--maxGreyFont);
  }

  .footer-orbit-poster {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    border-radius: 50%;
  }

  .footer-orbit canvas {
    display: block;
    width: 100%;
    height: 100%;
  }

  .footer-orbit-fallback {
    display: block;
    width: 100%;
    height: 100%;
  }
</style>
