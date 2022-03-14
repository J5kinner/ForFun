const readline = require("readline");

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});

let first = true;
let allMumble = true;
let fishy = false;
rl.on("line", (line) => {
  if (first) {
    n = +line;
    first = false;
    return;
  }
  const array = line.split(" ");
  for (let i = 0; i < n; i++) {
    if (
      parseInt(array[i]) !== i + 1 &&
      array[i] !== "mumble" &&
      parseInt(array[i]) !== undefined &&
      array[i] !== undefined &&
      !first
    ) {
      fishy = true;
    }
    if (array[i] != "mumble") {
      allMumble = false;
    }
  }
  if (allMumble) {
    console.log("makes sense");
    return;
  }
  if (fishy) {
    console.log("something is fishy");
  } else {
    console.log("makes sense");
  }
});
