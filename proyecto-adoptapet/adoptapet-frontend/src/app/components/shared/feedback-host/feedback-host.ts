import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedbackService } from '../../../services/feedback.service';

@Component({
  selector: 'app-feedback-host',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './feedback-host.html',
})
export class FeedbackHost {
  toasts$;
  confirm$;

  constructor(public feedback: FeedbackService) {
    this.toasts$ = this.feedback.toasts$;
    this.confirm$ = this.feedback.confirm$;
  }
}
