import {Routes} from '@angular/router';
import {Me} from './component/me/me';
import {Login} from './component/login/login';
import {Invite} from './component/invite/invite';
import {ShoppingLists} from './component/shopping-lists/shopping-lists';
import {Lists} from './component/lists/lists';

export const routes: Routes = [
  {path: 'me', component: Me},
  {path: 'login', component: Login},
  {path: 'invite', component: Invite},
  {path: 'lists', component: Lists},
  {path: 'shopping-lists', component: ShoppingLists}
];
