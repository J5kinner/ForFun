"use strict";

const fs = require("fs");

process.stdin.resume();
process.stdin.setEncoding("utf-8");

let inputString = "";
let currentLine = 0;

process.stdin.on("data", (inputStdin) => {
  inputString += inputStdin;
});

process.stdin.on("end", function () {
  inputString = inputString
    .replace(/\s*$/, "")
    .split("\n")
    .map((str) => str.replace(/\s*$/, ""));

  main();
});

function readLine() {
  return inputString[currentLine++];
}

// Complete the minimumSwaps function below.
function minimumSwaps(arr) {
  //let a, b, count, swapArr

  //find the smallest number and swap it with the starting number
  //then move onto the next location and repeat the above
  let swapCount = 0;
  let sortedArr = arr;

  for (var i = 0; i < arr.length; i++) {
    const answer = i + 1;
    if (sortedArr[i] !== answer) {
      const swapIndex = arr.indexOf(answer, i);
      //swap the answer in and put the other number back
      sortedArr[swapIndex] = sortedArr[i];
      sortedArr[i] = answer;
      swapCount++;
    }
  }
  return swapCount;
}

function main() {
  const ws = fs.createWriteStream(process.env.OUTPUT_PATH);

  const n = parseInt(readLine(), 10);

  const arr = readLine()
    .split(" ")
    .map((arrTemp) => parseInt(arrTemp, 10));

  const res = minimumSwaps(arr);

  ws.write(res + "\n");

  ws.end();
}
