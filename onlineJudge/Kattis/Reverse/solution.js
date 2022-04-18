const readline = require("readline");

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});

let arr = new Array();
let count = 0;
let first = true;
let size = 0;
rl.on("line", (line) => {
  if (first) {
    size = parseInt(line);
    first = false;
  } else {
    const element = line
    arr.push(element);
    count++;
  }
  if (size === count) {
    for (let i = size-1; i >= 0; i--) {
      console.log(arr[i]);
    }
    rl.close();
  }
});
