/*等 HTML 页面加载完成后再执行里面的代码,确保页面上的按钮和链接等元素已经存在 */
document.addEventListener("DOMContentLoaded", () => {  
  /*查找页面中所有带有 data-confirm 属性的元素*/
  document.querySelectorAll("[data-confirm]").forEach((button) => {
    /*给每个带data-confirm的元素绑定点击事件*/
    button.addEventListener("click", (event) => {
      /*读取元素上的data-confirm内容，作为确认框里的提示文字*/
      const message = button.getAttribute("data-confirm");
      /* message:确认提示不为空; window.confirm(message)会弹出浏览器确认框
      用户点“确定”时返回true; 用户点“取消”(!window.confirm(message))时返回false*/
      if (message && !window.confirm(message)) {
        event.preventDefault();/*阻止默认行为*/
      }
    });
  });
});
