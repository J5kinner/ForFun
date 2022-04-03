const readline = require("readline");

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});
let startVars;
let jackSize;
let jillSize;
let jackMap = new Map();
let cdsOwned = 0;

rl.once("line", (line) => {
  startVars = line.split(" ");
  jackSize = parseInt(startVars[0]);
  jillSize = parseInt(startVars[1]);
  let jackCount = jackSize;
  let jillCount = jillSize;
  rl.on("line", (line) => {
    const str = line;
    if (str === "0 0") {
        // console.log("we done");
        rl.close();
      }
    if (jillCount > 0 && jackCount === 0) {
      if (jackMap.has(str)) {
        cdsOwned++;
        // console.log("Finding " + str);
      }
      jillCount--;
    }
    if (jackCount > 0) {
      jackMap.set(str);
      //   console.log("Storing " + str);
      jackCount--;
    }
  }).on("close", () => {
    console.log(cdsOwned);
  });
});

