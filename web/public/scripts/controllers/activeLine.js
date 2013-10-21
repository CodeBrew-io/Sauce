// var WRAP_CLASS = "activeline";
// var BACK_CLASS = "activeline";
// var pos = cm.getCursor().line

// updateActiveLine(cm, pos);
// updateActiveLine(cmInsight, pos);

// function clearActiveLine(cm) {
//   if ("activeLine" in cm.state) {
//     cm.removeLineClass(cm.state.activeLine, "wrap", WRAP_CLASS);
//     cm.removeLineClass(cm.state.activeLine, "background", BACK_CLASS);
//   }
// }

// function updateActiveLine(cm, pos) {
//   var line = cm.getLineHandleVisualStart(pos);
//   if (cm.state.activeLine == line) return;
//   clearActiveLine(cm);
//   cm.addLineClass(line, "wrap", WRAP_CLASS);
//   cm.addLineClass(line, "background", BACK_CLASS);
//   cm.state.activeLine = line;
// }