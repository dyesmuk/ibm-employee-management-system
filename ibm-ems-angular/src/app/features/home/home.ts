import { Component } from '@angular/core';
import { UserService } from '../../core/services/user/user.service';

@Component({
  selector: 'app-home',
  imports: [],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {

  user: any;

  constructor(private userService: UserService) { }

  // here 

  ngOnInit() {
    this.userService.getUserById().subscribe((data) => {
      this.user = data;
      console.log(this.user);
    });
  }

}


