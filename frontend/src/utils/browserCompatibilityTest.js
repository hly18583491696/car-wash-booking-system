/**
 * 浏览器兼容性测试工具
 * 用于检测管理页面在不同浏览器中的兼容性
 */

class BrowserCompatibilityTest {
  constructor() {
    this.testResults = [];
    this.userAgent = navigator.userAgent;
    this.browserInfo = this.getBrowserInfo();
  }

  /**
   * 获取浏览器信息
   */
  getBrowserInfo() {
    const ua = navigator.userAgent;
    let browser = "Unknown";
    let version = "Unknown";

    if (ua.indexOf("Chrome") > -1 && ua.indexOf("Edge") === -1) {
      browser = "Chrome";
      version = ua.match(/Chrome\/(\d+)/)?.[1] || "Unknown";
    } else if (ua.indexOf("Firefox") > -1) {
      browser = "Firefox";
      version = ua.match(/Firefox\/(\d+)/)?.[1] || "Unknown";
    } else if (ua.indexOf("Safari") > -1 && ua.indexOf("Chrome") === -1) {
      browser = "Safari";
      version = ua.match(/Version\/(\d+)/)?.[1] || "Unknown";
    } else if (ua.indexOf("Edge") > -1) {
      browser = "Edge";
      version = ua.match(/Edge\/(\d+)/)?.[1] || "Unknown";
    }

    return { browser, version, userAgent: ua };
  }

  /**
   * 测试CSS Grid支持
   */
  testCSSGrid() {
    const testElement = document.createElement("div");
    testElement.style.display = "grid";
    const supported = testElement.style.display === "grid";

    this.testResults.push({
      test: "CSS Grid Support",
      supported,
      critical: true,
    });

    return supported;
  }

  /**
   * 测试Flexbox支持
   */
  testFlexbox() {
    const testElement = document.createElement("div");
    testElement.style.display = "flex";
    const supported = testElement.style.display === "flex";

    this.testResults.push({
      test: "Flexbox Support",
      supported,
      critical: true,
    });

    return supported;
  }

  /**
   * 测试CSS变量支持
   */
  testCSSVariables() {
    const supported =
      window.CSS && CSS.supports && CSS.supports("color", "var(--test)");

    this.testResults.push({
      test: "CSS Variables Support",
      supported,
      critical: false,
    });

    return supported;
  }

  /**
   * 测试ES6支持
   */
  testES6Support() {
    let supported = true;

    try {
      // 测试箭头函数
      eval("() => {}");
      // 测试模板字符串
      eval("`test`");
      // 测试解构赋值
      eval("const {a} = {a: 1}");
    } catch (e) {
      supported = false;
    }

    this.testResults.push({
      test: "ES6 Support",
      supported,
      critical: true,
    });

    return supported;
  }

  /**
   * 测试Fetch API支持
   */
  testFetchAPI() {
    const supported = typeof fetch !== "undefined";

    this.testResults.push({
      test: "Fetch API Support",
      supported,
      critical: true,
    });

    return supported;
  }

  /**
   * 测试Promise支持
   */
  testPromiseSupport() {
    const supported = typeof Promise !== "undefined";

    this.testResults.push({
      test: "Promise Support",
      supported,
      critical: true,
    });

    return supported;
  }

  /**
   * 测试localStorage支持
   */
  testLocalStorage() {
    let supported = false;

    try {
      const testKey = "__test__";
      localStorage.setItem(testKey, "test");
      localStorage.removeItem(testKey);
      supported = true;
    } catch (e) {
      supported = false;
    }

    this.testResults.push({
      test: "localStorage Support",
      supported,
      critical: false,
    });

    return supported;
  }

  /**
   * 测试DOM操作性能
   */
  async testDOMPerformance() {
    const startTime = performance.now();

    // 创建大量DOM元素测试
    const container = document.createElement("div");
    for (let i = 0; i < 1000; i++) {
      const element = document.createElement("div");
      element.textContent = `Test Element ${i}`;
      container.appendChild(element);
    }

    document.body.appendChild(container);

    const endTime = performance.now();
    const duration = endTime - startTime;

    // 清理测试元素
    document.body.removeChild(container);

    const performanceGood = duration < 100; // 100ms内完成认为性能良好

    this.testResults.push({
      test: "DOM Performance",
      supported: performanceGood,
      critical: false,
      details: `${duration.toFixed(2)}ms`,
    });

    return { duration, performanceGood };
  }

  /**
   * 测试CSS动画支持
   */
  testCSSAnimations() {
    const testElement = document.createElement("div");
    const supported =
      "animation" in testElement.style ||
      "webkitAnimation" in testElement.style ||
      "mozAnimation" in testElement.style;

    this.testResults.push({
      test: "CSS Animations Support",
      supported,
      critical: false,
    });

    return supported;
  }

  /**
   * 运行所有兼容性测试
   */
  async runAllTests() {
    console.log("🚀 开始浏览器兼容性测试...");
    console.log(
      `浏览器信息: ${this.browserInfo.browser} ${this.browserInfo.version}`,
    );

    // 运行所有测试
    this.testCSSGrid();
    this.testFlexbox();
    this.testCSSVariables();
    this.testES6Support();
    this.testFetchAPI();
    this.testPromiseSupport();
    this.testLocalStorage();
    this.testCSSAnimations();
    await this.testDOMPerformance();

    return this.generateReport();
  }

  /**
   * 生成测试报告
   */
  generateReport() {
    const criticalIssues = this.testResults.filter(
      (result) => !result.supported && result.critical,
    );
    const warnings = this.testResults.filter(
      (result) => !result.supported && !result.critical,
    );
    const passed = this.testResults.filter((result) => result.supported);

    const report = {
      browserInfo: this.browserInfo,
      summary: {
        total: this.testResults.length,
        passed: passed.length,
        criticalIssues: criticalIssues.length,
        warnings: warnings.length,
      },
      results: this.testResults,
      compatible: criticalIssues.length === 0,
    };

    // 输出报告到控制台
    console.log("\n📊 兼容性测试报告:");
    console.log(`✅ 通过: ${passed.length}/${this.testResults.length}`);
    console.log(`❌ 严重问题: ${criticalIssues.length}`);
    console.log(`⚠️  警告: ${warnings.length}`);

    if (criticalIssues.length > 0) {
      console.log("\n🚨 严重兼容性问题:");
      criticalIssues.forEach((issue) => {
        console.log(`  - ${issue.test}: 不支持`);
      });
    }

    if (warnings.length > 0) {
      console.log("\n⚠️  兼容性警告:");
      warnings.forEach((warning) => {
        console.log(`  - ${warning.test}: 不支持 ${warning.details || ""}`);
      });
    }

    console.log(
      `\n🎯 总体兼容性: ${report.compatible ? "✅ 兼容" : "❌ 不兼容"}`,
    );

    return report;
  }

  /**
   * 测试管理页面特定功能
   */
  async testAdminPageFeatures() {
    console.log("\n🔧 测试管理页面特定功能...");

    const features = [];

    // 测试表格渲染
    try {
      const table = document.querySelector(".el-table");
      features.push({
        feature: "表格渲染",
        working: !!table,
        element: table,
      });
    } catch (e) {
      features.push({
        feature: "表格渲染",
        working: false,
        error: e.message,
      });
    }

    // 测试侧边栏
    try {
      const sidebar = document.querySelector(".sidebar");
      features.push({
        feature: "侧边栏",
        working: !!sidebar,
        element: sidebar,
      });
    } catch (e) {
      features.push({
        feature: "侧边栏",
        working: false,
        error: e.message,
      });
    }

    // 测试标签切换
    try {
      const tabs = document.querySelectorAll(".menu-item");
      features.push({
        feature: "标签切换",
        working: tabs.length > 0,
        count: tabs.length,
      });
    } catch (e) {
      features.push({
        feature: "标签切换",
        working: false,
        error: e.message,
      });
    }

    console.log("管理页面功能测试结果:");
    features.forEach((feature) => {
      const status = feature.working ? "✅" : "❌";
      console.log(`  ${status} ${feature.feature}`);
      if (feature.error) {
        console.log(`    错误: ${feature.error}`);
      }
    });

    return features;
  }
}

// 导出测试类
export default BrowserCompatibilityTest;

// 如果在浏览器环境中直接运行
if (typeof window !== "undefined") {
  window.BrowserCompatibilityTest = BrowserCompatibilityTest;

  // 自动运行测试（可选）
  if (window.location.search.includes("runCompatibilityTest=true")) {
    const tester = new BrowserCompatibilityTest();
    tester.runAllTests().then((report) => {
      console.log("完整测试报告:", report);
    });
  }
}
