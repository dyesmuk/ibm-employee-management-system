import { Component, OnInit } from '@angular/core';
import { UserService } from '../../core/services/user/user.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  imports: [FormsModule, CommonModule],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit {

  constructor(private userService: UserService) { }

  id: number | undefined;
  user: any;        // ← search result
  userData: any;    // ← on load result

  ngOnInit() {
    this.userService.getUserById().subscribe((data) => {
      this.userData = data;                    // ← on load data ✅
      console.log(this.userData?.username);
    });
  }

  getById() {
    if (!this.id) return;

    this.userService.getUserById(this.id).subscribe((data) => {
      this.user = data;                        // ← search data ✅
      console.log(this.user?.username);
    });

    this.id = undefined;
  }
}