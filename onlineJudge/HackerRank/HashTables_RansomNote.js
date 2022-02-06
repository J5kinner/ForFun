"use strict";

process.stdin.resume();
process.stdin.setEncoding("utf-8");

let inputString = "";
let currentLine = 0;

process.stdin.on("data", function (inputStdin) {
  inputString += inputStdin;
});

process.stdin.on("end", function () {
  inputString = inputString.split("\n");

  main();
});

function readLine() {
  return inputString[currentLine++];
}

/*
 * Complete the 'checkMagazine' function below.
 *
 * The function accepts following parameters:
 *  1. STRING_ARRAY magazine
 *  2. STRING_ARRAY note
 */
let bool = true;
function checkMagazine(magazine, note) {
  // Write your code here
  //magazine: dictionary of words we can use
  //note: what we are checking against to make sure it is correct (case sensitive)

  //Split each line by spaces and fill hash maps for both
  //compare magHash with noteHash
  //if theres a new word in note (case sensitive words included) -> NO
  //if every word in note exists in mag -> YES

    let map = { };
    let replicable = true;
    for (let i of magazine) {
        map[i] = (map[i] || 0) + 1;
    }
    for (let i of note) {
        map[i] = (map[i] || 0) - 1;
    }
    for (let i in map) {
        if(!replicable) {
            break;
        } else if(map[i] < 0) {
            replicable = false;
        }
    }
  //   console.log(map);
    console.log(replicable ? "Yes" : "No");
}

function main() {
  const firstMultipleInput = readLine().replace(/\s+$/g, "").split(" ");

  const m = parseInt(firstMultipleInput[0], 10);

  const n = parseInt(firstMultipleInput[1], 10);

  const magazine = readLine().replace(/\s+$/g, "").split(" ");

  const note = readLine().replace(/\s+$/g, "").split(" ");

  checkMagazine(magazine, note);
}
