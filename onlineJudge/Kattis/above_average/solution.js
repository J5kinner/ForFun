function calcMark(marks) {
    let size = marks[0];
    let total = 0;
    
    for(let i=1; i<size; i++){
        total += marks[i]
    }
    let avg = (total / size);
    console.log(avg);
    return avg;
}
const readline = require("readline");

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});

rl.on("line", (line) => {
    console.log(line)
    const students = line.split(" ");
    console.log(calcMark(students));
``
  rl.close();
});
