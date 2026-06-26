import { Component } from '@angular/core';

@Component({
  selector: 'app-youtube',
  standalone: true,
  template: `
    <section id="tutorial" class="py-24 px-6 bg-[#0f1117]">
      <div class="max-w-4xl mx-auto text-center">
        <h2 class="text-4xl font-bold text-[#e4e4e7] mb-4">Watch It in Action</h2>
        <p class="text-[#9ca3af] text-lg mb-12">See how easy it is to go from a blank CSV to a complete overtime analysis.</p>

        <!-- Placeholder video frame -->
        <div class="relative rounded-xl overflow-hidden border border-[#2a2d3a] bg-[#1a1d27] aspect-video flex items-center justify-center group cursor-pointer">
          <div class="absolute inset-0 bg-gradient-to-br from-[#6366f1]/5 to-transparent"></div>
          <!-- Play button -->
          <div class="relative z-10 flex flex-col items-center gap-4">
            <div class="w-20 h-20 bg-[#6366f1] rounded-full flex items-center justify-center shadow-lg shadow-[#6366f1]/30 group-hover:scale-110 transition-transform">
              <svg class="w-8 h-8 text-white ml-1" fill="currentColor" viewBox="0 0 24 24">
                <path d="M8 5v14l11-7z"/>
              </svg>
            </div>
            <div>
              <p class="text-[#e4e4e7] font-semibold text-lg">Tutorial Coming Soon</p>
              <p class="text-[#9ca3af] text-sm mt-1">Full walkthrough video in preparation</p>
            </div>
          </div>
          <!-- Grid lines decoration -->
          <div class="absolute inset-0 opacity-5" style="background-image: linear-gradient(#6366f1 1px, transparent 1px), linear-gradient(90deg, #6366f1 1px, transparent 1px); background-size: 40px 40px;"></div>
        </div>

        <p class="text-[#9ca3af] text-sm mt-6">
          Want to be notified when the tutorial is live?
          <a href="#download" class="text-[#6366f1] hover:underline ml-1">Download the app</a> and check back soon.
        </p>
      </div>
    </section>
  `
})
export class YoutubeComponent {}
