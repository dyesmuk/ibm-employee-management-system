import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Demo } from './components/demo/demo';
import { Employee } from './components/employee/employee';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Demo, Employee],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('ibm-ems-angular');
  username = "Vaman";
}

