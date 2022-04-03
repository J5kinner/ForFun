const readline = require("readline");

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});
const arr = new Array();

rl.on("line", (line) => {
  const str = line.split();
  arr.push(str);
}).on("close", () => {
  //once all the data has been read
  let x = 0;
  arr.forEach((str) => {
    if (x % 2 !== 0) {
      console.log(str.toString());
    }
    x++;
  });
});
