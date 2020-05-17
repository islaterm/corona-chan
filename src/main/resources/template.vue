<template>
    <div class="corona">
        <h1>{{ msg }}</h1>
        <div id="minsal">
            <h2>Datos MINSAL</h2>
            <ul>
                ~graphics~
            </ul>
            ~footnote~
        </div>

        <div id="foreword">
            <h2>Más cosas por venir...</h2>¿Quieres ayudar? ¿Tienes sugerencias?
            <br/>
            <a href="https://github.com/islaterm/corona-chan">
                <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Octicons-mark-github.svg/200px-Octicons-mark-github.svg.png"
                     alt="octocat" height="48" width="48" vspace="2"/>
            </a>
            <br/>
            <a href="https://github.com/islaterm/corona-chan">Visita a Corona-chan en GitHub</a>
        </div>
    </div>
</template>

<script>
// import CasosTotales from "./CasosTotales.vue";
export default {
    name: "CoronaChan",
    props: {
        msg: String
    }
};
</script>

        <!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h3 {
    margin: 40px 0 0;
}

ul {
    list-style-type: none;
    padding: 0;
}

li {
    display: inline-block;
    margin: 0 10px;
}

a {
    color: #42b983;
}
</style>
