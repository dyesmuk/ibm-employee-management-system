import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-child',
  imports: [],
  templateUrl: './child.html',
  styleUrl: './child.css',
})
export class Child {

  // parent to  child
  @Input()
  dataFromParent = "";

  // child to parent 
  childData = "Monu";

  @Output()
  notify = new EventEmitter<string>();

  sendDataToParent = () => {
    console.log("sendDataToParent", this.childData);
    this.notify.emit(this.childData);
  }

}



