function test() {
    console.log(1 == '1')
    console.log(1 === '1')


}    
console.log(test());

class Student {
    constructor(name){
        this.name = name;
    }
}

class Person extends Student {
    constructor(name, id) {
        super(name)
        this.ID = id
    }
}

const jonah = new Person("jonah", 42);
console.log(jonah);
