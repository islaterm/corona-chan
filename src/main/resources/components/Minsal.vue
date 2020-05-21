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