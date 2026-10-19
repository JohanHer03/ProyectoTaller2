// Selector de cantidad y total de la compra en vivo
(function () {
  const form = document.getElementById("form-compra");

  if (!form) {
    return;
  }

  const totalEl = document.getElementById("total");
  const resumenEl = document.getElementById("resumen");
  const formato = new Intl.NumberFormat("es-CO", { maximumFractionDigits: 0 });

  function limitar(input) {
    const max = Number(input.max) || 0;
    let valor = Math.floor(Number(input.value) || 0);

    valor = Math.min(Math.max(valor, 0), max);
    input.value = valor;

    return valor;
  }

  function actualizar() {
    let total = 0;
    let unidades = 0;

    form.querySelectorAll(".producto").forEach(function (fila) {
      const input = fila.querySelector("input");
      const cantidad = limitar(input);

      total += cantidad * Number(fila.dataset.precio);
      unidades += cantidad;

      fila.classList.toggle("elegido", cantidad > 0);
    });

    totalEl.textContent = "$" + formato.format(total);
    resumenEl.textContent =
      unidades === 0
        ? "Aún no has elegido productos"
        : unidades === 1
          ? "Total por 1 unidad"
          : "Total por " + unidades + " unidades";
  }

  form.addEventListener("click", function (evento) {
    const boton = evento.target.closest("[data-paso]");

    if (!boton) {
      return;
    }

    const input = boton.parentElement.querySelector("input");

    input.value = (Number(input.value) || 0) + Number(boton.dataset.paso);
    actualizar();
  });

  form.addEventListener("input", actualizar);

  actualizar();
})();
