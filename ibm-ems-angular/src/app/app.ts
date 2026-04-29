import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Demo } from './components/demo';
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Demo],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('ibm-ems-angular');
  username = "Vaman";
}

