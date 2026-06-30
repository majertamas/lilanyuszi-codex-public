import {Injectable, signal, WritableSignal} from '@angular/core';

@Injectable({providedIn: 'root'})
export class BlockUiService {
  blocked: WritableSignal<boolean> = signal<boolean>(false)
}
