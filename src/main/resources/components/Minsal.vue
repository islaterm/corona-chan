<template>
    <v-card class="d-inline-block mb-2 mx-xs-0 mx-2" elevation="0">
        <v-col id="DATOS_MINSAL" class="mb-5" cols="12" justify="center">
            <h2 class="headline font-weight-bold mb-3">
                Información oficial del Ministerio de Salud
            </h2>
            <v-row class="mb-5" align="center" justify="center">
                <Infections />
                <Quarantines />
            </v-row>

            <v-row id="FOOTNOTE" justify="center">
                <span class="mx-2" v-html="footnote.html"></span>
            </v-row>
        </v-col>
    </v-card>
</template>

<script>
    import Infections from "./minsal/Infections"
    import Quarantines from "./minsal/Quarantines"

    export default {
        name: 'Minsal',
        components: {
            Infections,
            Quarantines
        },
        data: () => ({
            footnote: {
                html: '{~ footnote ~}'
            }
        }),
    }
</script>