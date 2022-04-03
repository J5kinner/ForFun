const readline = require("readline");

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});

rl.on("line", (line) => {
  const str = line;
  let i = 0;
  // console.log(str.split('').reverse());
  const revStr = str.split("");
  let ans = new Array();
  revStr.forEach((x) => {
    if (x === ")") {
      if (revStr[i - 1] === "-") {
        if (revStr[i - 2] === ":" || revStr[i - 2] === ";") {
          console.log(i - 2);
        }
      }
      if (revStr[i - 1] === ":" || revStr[i - 1] === ";") {
        console.log(i - 1);
      }
    }

    i++;
  });
  rl.close();
});
