import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { FeedbackHost } from './components/shared/feedback-host/feedback-host';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, FeedbackHost],
  templateUrl: './app.html',
  styleUrls: ['./app.css'],
})
export class App {}
