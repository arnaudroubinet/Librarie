import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavigationComponent } from './components/navigation.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavigationComponent],
  template: `
    <app-navigation>
      <router-outlet />
    </app-navigation>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'MotsPassants';
}
