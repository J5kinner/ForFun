//spot the bug problem
function calcPrice(price, tax, discount) {
    tax = tax || 0.05;
    discount = discount || 0;
  
    return price - price * discount + price * tax;
  }
console.log(calcPrice(10));

//no absolution or negative checker
//no variable initialisation or declaration without types

//Answer: 
/*The || operator checks for falsy values and 
* 0 is a falsy value, which would accidentally 
* apply the default tax rate. To fix this, instead
* of checking for a falsy value, you can use the nullish coalescing (??) operator
*/
