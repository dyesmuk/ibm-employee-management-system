
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
    selector: 'app-demo',
    imports: [RouterOutlet],
    templateUrl: './demo.html'
})
export class Demo {

    demoData = { fullName: 'Sonu Pande', salary: 10.25 };

}



// import { Component } from '@angular/core';
// import { RouterOutlet } from '@angular/router';

// @Component({
//     selector: 'app-demo',
//     imports: [],
//     template: `<div>
//     <h1>Demo Component</h1>
//     <p>This is demo component.</p>
// </div>`,
// // styles: 'style="color: blue"',
// // styleUrl: 'styles.css',
// // styleUrls: ['styles1.css', 'styles2.css']
// })

// export class Demo {

// }





