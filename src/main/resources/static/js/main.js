// main.js — improved interactions, keyboard-friendly scroller

console.log("SmartBank frontend loaded");


function toggleNav() {

  const nav = document.getElementById('mainNav');

  if (!nav) return;

  nav.classList.toggle('open');

}


// horizontal scroll keyboard navigation & small reveal animation

document.addEventListener('DOMContentLoaded', function () {

  const scroller = document.querySelector('.features-scroll');

  if (scroller) {

    scroller.addEventListener('keydown', function (ev) {

      if (ev.key === 'ArrowRight') scroller.scrollBy({ left: 300, behavior: 'smooth' });

      if (ev.key === 'ArrowLeft') scroller.scrollBy({ left: -300, behavior: 'smooth' });

    });


    // reveal cards

    const cards = scroller.querySelectorAll('.feature-card');

    const obs = new IntersectionObserver((entries) => {

      entries.forEach(e => {

        if (e.isIntersecting) {

          e.target.style.opacity = 1;

          e.target.style.transform = 'translateY(0)';

        }

      });

    }, { threshold: 0.25 });


    cards.forEach(c => {

      c.style.opacity = 0;

      c.style.transform = 'translateY(12px)';

      obs.observe(c);

    });

  }

});

