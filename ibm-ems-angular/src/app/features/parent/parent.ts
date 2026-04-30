import { Component } from '@angular/core';
import { Child } from '../child/child';

@Component({
  selector: 'app-parent',
  imports: [Child],
  templateUrl: './parent.html',
  styleUrl: './parent.css',
})
export class Parent {

  // parent to  child
  parentData = "Sonu";

  // child to parent 
  childDataInParent = "";

  receiveDataFromChild(message: string) {
    console.log("receiveDataFromChild", message);
    this.childDataInParent = message;
  }


}

