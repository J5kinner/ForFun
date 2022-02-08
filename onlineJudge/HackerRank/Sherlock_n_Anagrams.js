'use strict';

const fs = require('fs');

process.stdin.resume();
process.stdin.setEncoding('utf-8');

let inputString = '';
let currentLine = 0;

process.stdin.on('data', function (inputStdin) {
    inputString += inputStdin;
});

process.stdin.on('end', function () {
    inputString = inputString.split('\n');

    main();
});

function readLine() {
    return inputString[currentLine++];
}

/*
 * Complete the 'sherlockAndAnagrams' function below.
 *
 * The function is expected to return an INTEGER.
 * The function accepts STRING s as parameter.
 */

function sherlockAndAnagrams(s) {
    // Write your code here
    //AnagramCount;
    //map of our anagrams;
    //Construct hash table of potential anagrams
let result = []
let count = 0
  for (let i = 0; i < s.length; i++) {
      for (let j = i + 1; j < s.length + 1; j++) {
          result.push(s.slice(i, j).split("").sort().join(""));
      }
  }
  for(let i=0;i<result.length-1;i++){
      for(let j=i+1;j<result.length;j++){
          if((result[i]==result[j]) && result[i].length === result[j].length ){
              count++;
                console.log("Comparing " + result[i] + " and "+ result[j]);

          } 
      }
  }
  return count
// }
//     //Find every possibility for the string selected and store it in map
//     let subsets = [];
//     const anagrams = new Map();
//     let count = 0;
//     for (let key of s) {
//         // console.log(key);
//          anagrams.set(key);
//     }
//     // console.log([...anagrams.entries()]);

//     for (let i = 0; i < s.length; i++) { //loop through whole string
//         for (let j = i + 1; j < s.length +1; j++) { //loop up to the current string we are on        
//             let subset = s.slice(i, j).split("").sort().join("");
//             // console.log("Before Checking: " + anagrams.get(subset));
//             if (anagrams.get(subset)) {
//                 console.log("subset found: " + subset + " and anagram has " + anagrams.get(subset) + " ++! ");

//                 count++;
//             }
            
//             anagrams.set(subset, subset);
//             // console.log(anagrams.get(subset));


//         }

//         console.log([...anagrams.keys()]);

//         console.log("Count is now " + count);
//     //    for(let x of anagrams) {
//     //        console.log(x);
//     //    }
//     }


}


function main() {
    const ws = fs.createWriteStream(process.env.OUTPUT_PATH);

    const q = parseInt(readLine().trim(), 10);

    for (let qItr = 0; qItr < q; qItr++) {
        const s = readLine();

        const result = sherlockAndAnagrams(s);

        ws.write(result + '\n');
    }

    ws.end();
}
