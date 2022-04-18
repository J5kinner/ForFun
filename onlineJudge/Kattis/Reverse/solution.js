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

  //   console.log(count + " : " + size);
  //   console.log(arr);
  if (size === count) {
    for (let i = size - 1; i >= 0; i--) {
      const print = parseInt(arr[i], 10);
      console.log(print);
    }
    rl.close();
  }
});
