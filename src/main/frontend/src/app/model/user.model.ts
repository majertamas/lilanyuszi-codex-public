import {Group} from './group.model';

export interface User {
  id: number;
  name: string;
  nickName: string;
  email: string;
  picture: string;
  groups: Group[]
}
