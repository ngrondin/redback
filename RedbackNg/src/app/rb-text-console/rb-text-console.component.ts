import { Component, Input } from '@angular/core';

@Component({
  selector: 'rb-text-console',
  templateUrl: './rb-text-console.component.html',
  styleUrls: ['./rb-text-console.component.css']
})
export class RbTextConsoleComponent {
  @Input('lines') lines: string[] = [];

}
