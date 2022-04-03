const readline = require("readline");

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});

rl.on("line", (line) => {
  const input = line.split("-");
  let ans = "";
  for (str of input) {
    ans += str[0];
  }
  console.log(ans);
  rl.close();
});
