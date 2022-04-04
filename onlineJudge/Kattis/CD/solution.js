const readline = require("readline");

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});
let startVars;
let ans = 0;
let jackSize;
let jillSize;
let jackMap = new Set();
// let solutions = new Array();

rl.once("line", (line) => {
  startVars = line.split(" ");
  jackSize = parseInt(startVars[0]);
  jillSize = parseInt(startVars[1]);
  let jackCount = jackSize;
  let jillCount = jillSize;
  rl.on("line", (line) => {
    const str = line;
    const newStrChk = line.split(" ");
    // console.log(newStrChk);
    if (newStrChk.length > 1) {
      // console.log("Heyy");
      //NEED TO SOLVE WHEN WE HAVE ANOTHER NEW LIST APPEAR UNDERNEATH A CURRENT LIST
      if (jackCount === 0 && jillCount === 0) {
        console.log(ans);
      }
      if (ans !== NaN) {
        // solutions.push(ans);
        ans = 0;
      }

      jackMap = new Set();
      jackCount = parseInt(newStrChk[0]);
      jillCount = parseInt(newStrChk[1]);
      // console.log("Reset: " + jackCount + " "+jillCount);
    }
    if (
      newStrChk.length > 1 &&
      parseInt(newStrChk[0]) === 0 &&
      parseInt(newStrChk[0]) === 0
    ) {
      //  console.log("we done");
      rl.close();
    }
    //   console.log("Count " + jillCount + " " + jackCount);

    if (jillCount > 0 && jackCount === 0) {
      if (jackMap.has(str)) {
        //  console.log("Found " + str);
        ans++;
      }
      jillCount--;
    }
    if (jackCount > 0) {
      jackMap.add(str);
      // console.log("Storing " + str);
      jackCount--;
    }
  }).on("close", () => {
    // const filter = new Set([...jillMap].filter(i => jackMap.has(i)))
    //console.log(filter.size);
    // solutions.push(filter.size);
    // solutions.forEach(x => console.log(x));
    // console.log(solutions);
  });
});
