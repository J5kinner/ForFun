const readline = require("readline");

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});

rl.on("line", (line) => {
  const str = line.split(" ");
  console.log(parseInt(str[0]) + parseInt(str[1]));

  rl.close();
});
