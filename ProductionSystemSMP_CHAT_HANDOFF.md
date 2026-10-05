# ProductionSystemSMP – Chat Handoff / Projekt Kontext

## Cél
Ez a dokumentum egy másik ChatGPT beszélgetés számára készült projekt-handoff.
A cél, hogy a projekt fejlesztése ugyanonnan folytatható legyen, ahol az előző chat abbahagyta.

Fontos:
- A felhasználó magyarul kommunikál.
- Kódcentrikus, konkrét, lépésenkénti segítséget szeret.
- Egy változtatás egyszerre, utána Build/Test ellenőrzés.
- Ne adjunk nagy, találgató refaktorokat.
- Ha egy konkrét kódhiba nem látható biztosan, kérjük el a releváns kódrészletet.
- Seeder/migráció jelenleg nem fókusz; elsősorban a C# kód javítása a cél.
- A projekt offline/local használatra készül.
- Nincs login/user/role architektúra.

## Projekt

GitHub: `NyariMilan0/ProductionSystem`
Branch: `main`
Solution: `ProductionSystemSMP`
Adatbázis: `productionsystem_smp`

Stack:
- C#
- Visual Studio 2022
- .NET 8
- WPF
- PostgreSQL
- pgAdmin
- xUnit
- EF Core

Fő projektek:
- `src/Radan.Domain`
- `src/Radan.Application`
- `src/Radan.Infrastructure`
- `src/Radan.Desktop`
- `Radan.Tests`

## Projekt célja
Radan 2024.1 `.drg` fájlok tömeges feldolgozása és indexelése,
készletfogyasztás/visszaállítás, termelési riportok és replacement tracking.

Fő funkciók:
- `.drg` fájlok feldolgozása
- anyagfelismerés
- készletből fogyasztás
- reversal / restore
- DRG material usage rögzítés
- replacement követés
- termelési riportok
- dashboard
- később scanner/worker

## Anyaglogika

Anyagkódok:
- FeZn
- Al99
- AlMg3
- A2
- A4

Radan mapping:
- Tűzihorganyzott variációk → `FeZn`
- Aluminium / Al99 → `Al99`
- AlMg3 → `AlMg3`
- A2 → `A2`
- A4 → `A4`

Felhasználói domain döntések:
- Radan nem kezeli a foil attribútumot üzleti logikaként.
- Rozsdamentes anyagok Radan oldalon A2 és A4 kategóriák.
- Nem kell teljes szabványszámokat mindenhol tárolni, elég lehet például A2 + 1.5 mm + 3000x1500.
- Aluminium (Al99) készleten csak 1.0 mm van.
- AlMg3 készleten: 1.5 / 2 / 3 mm.
- Az A4 pontos szabványszámát ne találgassuk.

## Fontos domain entitások

### InventoryItem
```csharp
public sealed class InventoryItem
{
    public long Id { get; set; }
    public long SheetSpecificationId { get; set; }
    public int ActualLengthMm { get; set; }
    public int ActualWidthMm { get; set; }
    public bool Foil { get; set; }
    public string? Surface { get; set; }
    public string? Quality { get; set; }
    public MaterialStockKind StockKind { get; set; } = MaterialStockKind.FullSheet;
    public bool IsActive { get; set; } = true;
    public DateTimeOffset CreatedAt { get; set; }
    public SheetSpecification? SheetSpecification { get; set; }
    public InventoryBalance? Balance { get; set; }
    public ICollection<InventoryLot> Lots { get; set; } = new List<InventoryLot>();
}
```

### DrgMaterialUsage
```csharp
public sealed class DrgMaterialUsage
{
    public long Id { get; set; }
    public long DrgJobId { get; set; }
    public long? InventoryItemId { get; set; }
    public long? MaterialGradeId { get; set; }
    public decimal? ThicknessMm { get; set; }
    public int? LengthMm { get; set; }
    public int? WidthMm { get; set; }
    public decimal QuantityUsed { get; set; }
    public int SequenceNo { get; set; }
    public string? MetadataJson { get; set; }
    public DrgJob? DrgJob { get; set; }
    public MaterialGrade? MaterialGrade { get; set; }
    public InventoryItem? InventoryItem { get; set; }
}
```

### Inventory service interface
```csharp
Task<long> ReceiveStockAsync(long inventoryItemId, decimal quantity, decimal? unitCostEur,
    string? supplier, string? receiptReference, string? notes,
    CancellationToken cancellationToken = default);

Task<long> ConsumeStockAsync(long inventoryItemId, decimal quantity,
    long? drgMaterialUsageId = null, long? sourceFileRevisionId = null,
    string? note = null, CancellationToken cancellationToken = default);

Task<long> RestoreStockAsync(long transactionId, string? note = null,
    CancellationToken cancellationToken = default);
```

### Material inventory resolver interface
```csharp
Task<InventoryItem?> FindMatchingInventoryItemAsync(
    string? radanMaterialCode,
    decimal thicknessMm,
    int lengthMm,
    int widthMm,
    CancellationToken cancellationToken = default);
```

## Domain entity kapcsolatok
`InventoryItem → SheetSpecification? → MaterialGradeThickness? → MaterialGrade?`

### MaterialGrade
```csharp
public sealed class MaterialGrade
{
    public long Id { get; set; }
    public long FamilyId { get; set; }
    public required string Code { get; set; }
    public required string DisplayName { get; set; }
    public string? Standard { get; set; }
    public decimal? DensityKgM3 { get; set; }
    public bool IsActive { get; set; } = true;
    public DateTimeOffset CreatedAt { get; set; }
    public MaterialFamily? Family { get; set; }
    public ICollection<MaterialGradeThickness> AllowedThicknesses { get; set; } = new List<MaterialGradeThickness>();
}
```

### MaterialGradeThickness
```csharp
public sealed class MaterialGradeThickness
{
    public long Id { get; set; }
    public long MaterialGradeId { get; set; }
    public decimal ThicknessMm { get; set; }
    public MaterialGrade? MaterialGrade { get; set; }
    public ICollection<SheetSpecification> SheetSpecifications { get; set; } = new List<SheetSpecification>();
}
```

### SheetSpecification
```csharp
public sealed class SheetSpecification
{
    public long Id { get; set; }
    public long MaterialGradeThicknessId { get; set; }
    public int LengthMm { get; set; }
    public int WidthMm { get; set; }
    public MaterialGradeThickness? MaterialGradeThickness { get; set; }
    public ICollection<InventoryItem> InventoryItems { get; set; } = new List<InventoryItem>();
}
```

## Eddigi javítások

### Step 1 – InventoryService concurrency ✅
Korábbi probléma: a balance külön olvasása és módosítása versenyhelyzetet okozhatott.

Javítás: Receipt / Consume / Restore a PostgreSQL oldali `apply_inventory_transaction(...)` helperre támaszkodik, Restore esetén az eredeti Consume tranzakció zárolásával.

User visszajelzés: tesztek sikeresek.

### Step 2 – MaterialGradeId ✅
Korábbi probléma: a `DrgMaterialUsage.MaterialGradeId` nem volt beállítva.

A resolver betölti:
`SheetSpecification → MaterialGradeThickness → MaterialGrade`

A processing logika sikeres inventory resolution után beállítja az InventoryItemId-t és a MaterialGradeId-t.

Teszt ellenőrzi:
```csharp
Assert.Equal(materialGrade.Id, usage.MaterialGradeId);
Assert.NotNull(usage.MaterialGrade);
Assert.Equal("FeZn", usage.MaterialGrade!.Code);
```

A user legutóbbi visszajelzése: **0 Error, 0 Warning, minden teszt átment.**

## Jelenlegi warningmentes MaterialInventoryResolver
A működő állapot lényege:
```csharp
var item = await _db.InventoryItems
    .AsNoTracking()
    .Include(x => x.SheetSpecification)
        .ThenInclude(x => x!.MaterialGradeThickness!)
            .ThenInclude(x => x!.MaterialGrade!)
    .Where(x =>
        x.IsActive &&
        x.StockKind == MaterialStockKind.FullSheet &&
        x.ActualLengthMm == canonicalLength &&
        x.ActualWidthMm == canonicalWidth &&
        x.Balance != null &&
        x.Balance!.QuantityOnHand > 0 &&
        x.SheetSpecification != null &&
        x.SheetSpecification!.LengthMm == canonicalLength &&
        x.SheetSpecification!.WidthMm == canonicalWidth &&
        x.SheetSpecification!.MaterialGradeThickness != null &&
        x.SheetSpecification!.MaterialGradeThickness!.ThicknessMm == thicknessMm &&
        x.SheetSpecification!.MaterialGradeThickness!.MaterialGrade != null &&
        x.SheetSpecification!.MaterialGradeThickness!.MaterialGrade!.Code == gradeCode)
    .OrderBy(x => x.CreatedAt)
    .ThenBy(x => x.Id)
    .FirstOrDefaultAsync(cancellationToken);
```

Ne módosítsuk újra indokolatlanul, mert a usernél jelenleg 0 warning / 0 error.

## Következő feladat – Step 3

### ReplacementService
Korábbi review alapján:
- `ReplacementService.CreateAsync` létrehozza a `Replacement` rekordot,
- de sikeres replacement létrehozáskor a parent `DrgJob` állapotát is frissíteni kell.

Elvárt állapot:
```csharp
DrgJob.IsReplacement = true;
DrgJob.ReplacementReason = ...;
```

A replacement rekord és a parent DrgJob frissítése lehetőleg ugyanabban a DB tranzakcióban történjen.

Továbbá idempotens újrafeldolgozásnál a `DrgJobPersistenceService` ne nullázza / írja felül a replacement adatokat.

Elsőként ezt a fájlt kell elkérni:
`ProductionSystemSMP/src/Radan.Infrastructure/Services/ReplacementService.cs`

Szükség esetén:
- `DrgJobPersistenceService.cs`
- `DrgJob` entity
- replacement application interface/DTO
- kapcsolódó replacement tesztek

## További review sorrend
1. InventoryService concurrency – KÉSZ
2. MaterialGradeId – KÉSZ
3. ReplacementService replacement state – KÖVETKEZŐ
4. Dashboard material usage count csak processed jobokra
5. duplicate/dead Application interfaces/models takarítása
6. PathRuleResolver duplikált path logika egységesítése
7. unknown path behavior
8. ProductionQueryService N+1
9. DI architecture tisztítása
10. Worker
11. scanner concurrency / file stability
12. pricing lots/FIFO
13. remnant inventory logika

## Egyéb ismert problémák
- korábban test target framework mismatch volt: `Radan.Tests` net10 / EF10 vs production net8
- duplicate `DbSet<Replacement>` lehetett a `ProductionSystemDbContext`-ben
- base schema és live migrations eltérés korábbi review alapján
- hiányzó ArticleNumber korábban csendben skipelődött
- Worker connection string mismatch

## Munkamenet
Minden lépésnél:
1. érintett fájl megtekintése
2. csak az adott hibát javítani
3. teljes cserélendő fájl vagy pontos blokk megadása
4. Build / Rebuild
5. Tesztek
6. csak siker esetén tovább a következő pontra

## Folytatási pont
**Jelenlegi állapot:** Step 1 ✅, Step 2 ✅, Step 3 ⏳

**Legutóbbi baseline:** 0 Error / 0 Warning / tesztek sikeres.

A következő chatben elsőként kérd el a `ReplacementService.cs` teljes tartalmát, és folytasd a Step 3 javítást.
