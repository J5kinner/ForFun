function tripString(word) {
    let ans = "";
    let wLength = word.length / 3; 
    //get the size of how words we have. 
    let arr = new Array();
    for(let i=0; i<word.length; i+=wLength){
        let inputWord = word.substring(i, i+wLength);
    
        if(arr.includes(inputWord)){
        
           return inputWord; 
        }
        arr.push(inputWord);
    }
}
const readline = require("readline");

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});

rl.on("line", (line) => {

  console.log(tripString(line));

  rl.close();
});
