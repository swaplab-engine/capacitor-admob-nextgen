import { registerPlugin } from '@capacitor/core';

import type { AdMobNextGenPlugin } from './definitions';

const AdMobNextGen = registerPlugin<AdMobNextGenPlugin>('AdMobNextGen', {
  web: () => import('./web').then((m) => new m.AdMobNextGenWeb()),
});

export * from './definitions';
export { AdMobNextGen };
