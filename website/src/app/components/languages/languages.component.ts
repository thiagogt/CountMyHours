import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-languages',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="py-24 px-6 bg-[#1a1d27]/40">
      <div class="max-w-6xl mx-auto text-center">
        <h2 class="text-4xl font-bold text-[#e4e4e7] mb-4">Available in 9 Languages</h2>
        <p class="text-[#9ca3af] text-lg mb-12 max-w-xl mx-auto">
          Switch languages in Settings and the entire app — including holiday calendars — updates instantly.
        </p>
        <div class="flex flex-wrap justify-center gap-3">
          @for (lang of languages; track lang.code) {
            <div class="bg-[#1a1d27] border border-[#2a2d3a] rounded-xl px-5 py-3 flex items-center gap-3 hover:border-[#6366f1]/40 transition-all">
              <span class="text-2xl">{{ lang.flag }}</span>
              <div class="text-left">
                <div class="text-[#e4e4e7] text-sm font-medium">{{ lang.name }}</div>
                <div class="text-[#9ca3af] text-xs">{{ lang.region }}</div>
              </div>
            </div>
          }
        </div>
      </div>
    </section>
  `
})
export class LanguagesComponent {
  languages = [
    { code: 'en', flag: '🇺🇸', name: 'English', region: 'United States' },
    { code: 'pt-BR', flag: '🇧🇷', name: 'Português', region: 'Brasil' },
    { code: 'es-ES', flag: '🇪🇸', name: 'Español', region: 'España' },
    { code: 'it-IT', flag: '🇮🇹', name: 'Italiano', region: 'Italia' },
    { code: 'ja-JP', flag: '🇯🇵', name: '日本語', region: 'Japan' },
    { code: 'zh-CN', flag: '🇨🇳', name: '中文', region: 'China' },
    { code: 'hi-IN', flag: '🇮🇳', name: 'हिन्दी', region: 'India' },
    { code: 'en-GB', flag: '🇬🇧', name: 'English', region: 'United Kingdom' },
    { code: 'en-CA', flag: '🇨🇦', name: 'English', region: 'Canada' }
  ];
}
