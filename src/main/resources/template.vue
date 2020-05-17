<template>
    <v-container>
        <v-row class="text-center">
            <v-col cols="12">
                <v-img :src="require('../assets/CoronaChan.png')" class="my-3" contain height="512" />
            </v-col>

            <v-col class="mb-4">
                <h1
                        class="display-2 font-weight-bold mb-3"
                >Corona-chan te cuenta todo lo que necesitas saber sobre el COVID-19 en Chile</h1>
            </v-col>

            <v-col class="mb-5" cols="12">
                <h2 class="headline font-weight-bold mb-3">Datos MINSAL</h2>

                <v-row justify="center">
                    <a
                            v-for="(next, i) in minsalCharts"
                            :key="i"
                            :href="next.href"
                            class="subheading mx-3"
                            target="_blank"
                    >{{ next.text }}</a>
                </v-row>
                <v-row justify="center">
                    ~footnote~
                </v-row>
            </v-col>
        </v-row>
        <v-row>
            <v-col cols="12">
                <a href="https://github.com/islaterm/corona-chan">
                    <v-img
                            src="https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Octicons-mark-github.svg/200px-Octicons-mark-github.svg.png"
                            class="my-3"
                            contain
                            height="48"
                    />
                </a>
                <p class="text-center">
                    <a href="https://github.com/islaterm/corona-chan">Visita a Corona-chan en GitHub</a>
                </p>
            </v-col>
        </v-row>
    </v-container>
</template>

<script>
export default {
    name: 'CoronaChan',

    data: () => ({
        minsalCharts: [
            ~graphics~
        ],
    }),
}
</script>
