import {Message} from './message.model';

export interface RestResponse<T> {
  messages?: Message[];
  data: T
}
