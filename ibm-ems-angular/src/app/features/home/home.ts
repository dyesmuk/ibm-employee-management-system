import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
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

  constructor(private userService: UserService, private cdr: ChangeDetectorRef) { }

  id: number | undefined;
  user: any;
  userData: any;

  ngOnInit() {
    this.userService.getUserById().subscribe((data) => {
      this.userData = data;
      this.cdr.detectChanges();
      console.log(this.userData?.username);
    });
  }

  getById() {
    if (!this.id) return;
    this.userService.getUserById(this.id).subscribe((data) => {
      this.user = data;
      this.cdr.detectChanges();
      console.log(this.user?.username);
    });
    this.id = undefined;
  }
}